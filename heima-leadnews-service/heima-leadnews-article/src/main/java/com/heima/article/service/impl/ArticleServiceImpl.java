package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ArticleConfigMapper;
import com.heima.article.mapper.ArticleContentMapper;
import com.heima.article.mapper.ArticleMapper;
import com.heima.article.service.ArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.constants.ArticleConstants;
import com.heima.model.common.constants.WmNewsMessageConstants;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, ApArticle> implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleContentMapper articleContentMapper;

    @Autowired
    private ArticleConfigMapper articleConfigMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;



    /**
     * 加载首页-加载更多-加载更新 三位一体
     *
     * @param dto  type=1 认为是加载更多，type=2表示加载更新
     * @param type
     * @return
     */
    @Override
    public ResponseResult load(ArticleHomeDto dto, Short type) {
       //TODO

        List<ApArticle> apArticles = articleMapper.loadArticleList(dto, type);
        // System.out.println("hello"+apArticles);
        //3.返回数据
        return ResponseResult.okResult(apArticles);
    }


    /**
     * 保存三剑客
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public ResponseResult save(ArticleDto dto) {
       //判空
        if(dto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"参数失效");
        }
        //文章id是否空值
        //空值=>创建新文章
        Long id;
        if(dto.getId()==null){
            //createNew
            ApArticle apArticle = new ApArticle();
            BeanUtils.copyProperties(dto,apArticle);

            //封装
            apArticle.setLikes(0);
            apArticle.setCollection(0);
            apArticle.setComment(0);
            apArticle.setViews(0);
            apArticle.setPublishTime(new Date());

            articleMapper.insert(apArticle);

            //创建文章config
            id = apArticle.getId();
            System.out.println("看看我能不能获得MBplus id!"+id);
            ApArticleConfig apArticleConfig = new ApArticleConfig();
            apArticleConfig.setArticleId(id);
            apArticleConfig.setIsComment(true);
            apArticleConfig.setIsDown(false);
            apArticleConfig.setIsForward(false);
            apArticleConfig.setIsDelete(false);
            articleConfigMapper.insert(apArticleConfig);

            //创建文章内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(id);
            apArticleContent.setContent(dto.getContent());
            articleContentMapper.insert(apArticleContent);

        }
        else {
            //非空 => 更改文章
            ApArticleContent apArticleContent = new ApArticleContent();
            id = dto.getId();
            apArticleContent.setArticleId(id);
            articleContentMapper.updateById(apArticleContent);

        }

        //往ES的消息队列发送消息, 让ES同步


        ApArticle apArticle = articleMapper.selectById(id);
        rabbitTemplate.convertAndSend(ArticleConstants.ARTICLE_ES_SYNC_TOPIC,
                ArticleConstants.ARTICLE_ES_SYNC_ROUTINGKEY,apArticle);

        //3.返回数据,要不要返回文章id ，因为wmnews表中需要
        return ResponseResult.okResult(id);
    }

}
