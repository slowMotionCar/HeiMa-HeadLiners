package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.heima.api.feign.ApArticleFeignClient;
import com.heima.audit.baidu.BaiduTextScan;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.constants.WmNewsMessageConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.*;
import com.heima.utils.threadlocal.WmThreadLocalUtil;
import com.heima.wemedia.mapper.*;
import com.heima.model.wemedia.dtos.UporDownDto;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.async.BlockingQueueFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    @Autowired
    private WmNewsTaskService wmNewsTaskService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Resource
    private WmNewsMapper wmNewsMapper;

    @Autowired
    private ApArticleFeignClient apArticleFeignClient;

    @Autowired
    private WmUserMapper wmUserMapper;

    @Autowired
    private WmChannelMapper channelMapper;


    /**
     * 查询文章列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult list(WmNewsPageReqDto dto) {
        // 1.非空判断
        dto.checkParam();
        // 2.分页查询
        IPage<WmNews> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> queryWrapper = Wrappers.lambdaQuery();
        // 2.1 根据状态查询
        if (dto.getStatus() != null) {
            queryWrapper.eq(WmNews::getStatus, dto.getStatus());
        }

        // 2.2 根据关键字查询
        if (!StringUtils.isEmpty(dto.getKeyword())) {
            queryWrapper.like(WmNews::getTitle, dto.getKeyword());
        }

        // 2.3 根据频道id查询
        if (dto.getChannelId() != null) {
            queryWrapper.eq(WmNews::getChannelId, dto.getChannelId());
        }

        // 2.4 根据开始和结束时间查询
        if (dto.getBeginPubdate() != null && dto.getEndPubdate() != null) {
            queryWrapper.between(WmNews::getPublishTime, dto.getBeginPubdate(), dto.getEndPubdate());
        }

        // 2.5 根据登录用户id查询
        queryWrapper.eq(WmNews::getUserId, WmThreadLocalUtil.getUser().getId());

        page = super.page(page, queryWrapper);

        // 3.返回数据结果
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        result.setData(page.getRecords());
        return result;
    }

    /**
     * 保存-修改-提交草稿为一体的方法
     * 主方法
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public ResponseResult submit(WmNewsDto dto) {

        // 判空处理
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmNews wmNews = new WmNews();
        // 检查dtoid
        // id为空=>新增
        if (dto.getId() == null) {

            log.info("新增");
            wmNews = getWmNews(dto);
            wmNewsMapper.insert(wmNews);

        }

        // id不为空=>修改
        else {

            log.info("修改");
            // 修改方法
            // 删除已关联素材关系
            LambdaQueryWrapper<WmNewsMaterial> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WmNewsMaterial::getNewsId, dto.getId());
            wmNewsMaterialMapper.delete(wrapper);
            // 封装
            wmNews = getWmNews(dto);
            wmNews.setId(dto.getId());
            wmNewsMapper.updateById(wmNews);

        }

        // 如果提交信息为草稿, 则返回
        if (dto.getStatus() == 0) {
            return ResponseResult.okResult("添加为草稿成功");
        }

        // 文章内容和素材库关联
        // 提取文章内容和素材
        Map<String, Object> txtImgMap = imgInTextConnectedWithLibrary(dto);
        // 添加到素材库
        addFigureToLibrary(wmNews, txtImgMap, (short) 0);


        // 封面和素材库关联
        Map<String, Object> coverImgMap = new HashMap<>();
        List<String> images = dto.getImages();
        coverImgMap.put("image", images);
        addFigureToLibrary(wmNews, coverImgMap, (short) 1);


        // 提取封面图片
        List<String> imageList = (List) txtImgMap.get("image");
        StringBuilder sb = new StringBuilder();
        if (dto.getType() == -1) {
            // 封面无图, 则选无图
            if (imageList.size() == 0) {
                wmNews.setType((short) 0);
                wmNewsMapper.updateById(wmNews);
            }
            // 封面1-2图, 则选1图
            if (imageList.size() >= 1 && imageList.size() < 3) {
                wmNews.setType((short) 1);
                wmNews.setImages(imageList.get(0));
                wmNewsMapper.updateById(wmNews);
            }
            // 封面>=3 图, 则选3 图
            if (imageList.size() >= 3) {
                wmNews.setType((short) 3);
                sb.append(imageList.get(0)).append(",");
                sb.append(imageList.get(1)).append(",");
                sb.append(imageList.get(2));
                wmNews.setImages(sb.toString());
                wmNewsMapper.updateById(wmNews);
            }
        }

        // 配置多线程
        ExamCallable newThread = new ExamCallable();
        newThread.setWmNews(wmNews);
        newThread.setTxtImgMap(txtImgMap);
        newThread.setWmNewsMapper(wmNewsMapper);
        // 开启多线程
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 4, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(2),
                Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

        try {
            tpe.submit(newThread);
            // 把审核后的文章提交给article
            submitToArticle(wmNews);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tpe.shutdown();
        }

        log.info("我在自媒体端发布了!");
        // 6.返回数据
        return ResponseResult.okResult("发布文章完成");
    }

    // 把审核后的文章提交给article
    private void submitToArticle(WmNews wmNews) {
        ArticleDto articleDto = new ArticleDto();
        BeanUtils.copyProperties(wmNews, articleDto);
        // articleDto的Id是wmNews的articleID
        articleDto.setId(wmNews.getArticleId());
        articleDto.setAuthorId(WmThreadLocalUtil.getUser().getId().longValue());
        articleDto.setLayout(wmNews.getType());
        // 根据userID查找名字
        WmUser wmUser = wmUserMapper.selectById(WmThreadLocalUtil.getUser().getId());
        String name = wmUser.getName();
        articleDto.setAuthorName(name);
        // 根据channelId查找channelName
        WmChannel wmChannel = channelMapper.selectById(wmNews.getChannelId());
        articleDto.setChannelName(wmChannel.getName());
        ResponseResult responseResult = apArticleFeignClient.save(articleDto);
        Long data = (Long) responseResult.getData();
        wmNews.setArticleId(data);
        wmNewsMapper.updateById(wmNews);
    }

    // 把图片添加素材库
    @Transactional
    public void addFigureToLibrary(WmNews wmNews, Map<String, Object> txtImgMap, Short integer) {
        // 解析图片,并添加素材库
        List<String> imageList = (List) txtImgMap.get("image");
        for (String imageItem : imageList) {

            // 根据url查询图片id
            LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WmMaterial::getUrl, imageItem);
            WmMaterial wmMaterial = wmMaterialMapper.selectOne(wrapper);
            Integer imgId = wmMaterial.getId();
            Integer newsId = wmNews.getId();
            WmNewsMaterial wmNewsMaterial = new WmNewsMaterial();
            // 把图片id和新闻id添加到表里
            wmNewsMaterial.setType(integer);
            wmNewsMaterial.setOrd((short) 0);
            wmNewsMaterial.setMaterialId(imgId);
            wmNewsMaterial.setNewsId(newsId);
            wmNewsMaterialMapper.insert(wmNewsMaterial);
        }
    }

    // 提取文章内容和素材
    // Map!
    @Transactional
    public Map<String, Object> imgInTextConnectedWithLibrary(WmNewsDto dto) {
        Map<String, Object> txtImgMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        List<String> imgList = new ArrayList<>();
        String content = dto.getContent();
        // 提取出标题, 后面checkporn可以用
        String title = dto.getTitle();
        sb.append(title);
        // 解析JSONArray
        List<Map> maps = JSONArray.parseArray(content, Map.class);
        // 遍历, 分别将图片和文字提取
        maps.forEach((mapItem) -> {
            String type = (String) mapItem.get("type");
            if ("text".equals(type)) {
                sb.append(mapItem.get("value"));
            }
            if ("image".equals(type)) {
                imgList.add((String) mapItem.get("value"));
            }
        });
        // 封装
        txtImgMap.put("text", sb.toString());
        txtImgMap.put("image", imgList);
        // 返回
        return txtImgMap;
    }

    // 转换WmNews封装格式
    private static WmNews getWmNews(WmNewsDto dto) {
        // 新增方法
        StringBuilder sb = new StringBuilder();
        // 封装
        WmNews wmNews = new WmNews();
        String title = dto.getTitle();
        Integer channelId = dto.getChannelId();
        Date publishTime = dto.getPublishTime();
        Short type = dto.getType();
        Short status = dto.getStatus();
        String labels = dto.getLabels();

        // 内容 - 需要转换格式
        String content = dto.getContent();

        // 多个,需要根据地址查询id
        List<String> images = dto.getImages();

        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime(new Date());

        wmNews.setTitle(title);
        wmNews.setChannelId(channelId);
        wmNews.setPublishTime(publishTime);
        if (type != -1) {
            wmNews.setType(type);
        }
        wmNews.setStatus(status);
        wmNews.setLabels(labels);
        wmNews.setContent(content);

        if (dto.getImages() != null) {
            images.forEach(sb::append);
            String ImageString = sb.toString();
            wmNews.setImages(ImageString);
        }

        return wmNews;
    }

    /**
     * 文章上下架
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNewsDto dto) {

        // dto传参为空
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "参数错误");
        }

        Integer id = dto.getId();
        WmNews wmNewsReturn = wmNewsMapper.selectById(id);

        // 文章不存在
        if (wmNewsReturn == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "自媒体端文章不存在");
        }

        // dto中article不存在(未上架)
        if (wmNewsReturn.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章还没有成功通过审核");
        }

        // 修改状态为dto的状态
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        wmNews.setEnable(dto.getEnable());
        wmNewsMapper.updateById(wmNews);

        // 推送消息给RabbitMQ
        UporDownDto uporDownDto = new UporDownDto();
        uporDownDto.setId(wmNewsReturn.getArticleId());
        uporDownDto.setEnable(dto.getEnable());
        rabbitTemplate.convertAndSend(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC, WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_ROUTINGKEY, uporDownDto);

        return ResponseResult.okResult("文章上下架完成");
    }
}
