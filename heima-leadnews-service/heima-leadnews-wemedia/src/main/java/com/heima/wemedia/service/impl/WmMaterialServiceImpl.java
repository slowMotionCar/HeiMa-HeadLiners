package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.utils.threadlocal.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;

    @Resource
    private WmMaterialMapper wmMaterialMapper;

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    /**
     * 素材上传
     *
     * @param file
     * @return
     */
    @Override
    @Transactional
    public ResponseResult upload(MultipartFile file) {
        try {
            // 1.上传图片到minio中
            // 获取文件名字
            String originalFilename = file.getOriginalFilename();
            String[] split = originalFilename.split("\\.");
            // System.out.println(split);
            // System.out.println(split[0]);
            // System.out.println(split[1]);
            // 对文件名字进行加密
            String uuidName = UUID.randomUUID().toString().replace("-", "");
            String upLoadName = uuidName + "." + split[1];

            // 获得文件输入流
            InputStream inputStream = file.getInputStream();
            // 上传文件 获得文件url
            String fileUrl = fileStorageService.uploadHtmlFile("material-", upLoadName, inputStream);

            // 获取信息
            WmMaterial wmMaterial = new WmMaterial();
            wmMaterial.setCreatedTime(new Date());
            wmMaterial.setUrl(fileUrl);
            wmMaterial.setType((short) 0);
            wmMaterial.setIsCollection((short) 0);
            // 获取id
            Integer id = WmThreadLocalUtil.getUser().getId();
            wmMaterial.setUserId(id);

            // 写入数据库
            wmMaterialMapper.insert(wmMaterial);

            // 封装返回数据
            return ResponseResult.okResult(wmMaterial);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询素材列表
     *
     * @param dto
     * @return
     */
    @Override
    public PageResponseResult list(WmMaterialDto dto) {

        // 分页
        PageHelper.startPage(dto.getPage(), dto.getSize());
        Page<WmMaterial> page = null;
        //判空
        dto.checkParam();
        //获取用户id
        Integer uid = WmThreadLocalUtil.getUser().getId();
        List<WmMaterial> wmMaterials = null;
        // 开启收藏模式,按照添加时间排序
        if (dto.getIsCollection() == 1) {
            LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WmMaterial::getIsCollection, dto.getIsCollection())
                    .eq(WmMaterial::getUserId,uid)
                    .orderByDesc(WmMaterial::getCreatedTime);
            wmMaterials = wmMaterialMapper.selectList(wrapper);
            page = (Page<WmMaterial>) wmMaterials;
        }
        // 如果是关闭, 默认收藏排前面
        if (dto.getIsCollection() == 0) {
            wmMaterials = wmMaterialMapper.list(uid);
            page = (Page<WmMaterial>) wmMaterials;
        }

        PageResponseResult pageResponseResult = new PageResponseResult();
        pageResponseResult.setCurrentPage(dto.getPage());
        pageResponseResult.setSize(dto.getSize());
        pageResponseResult.setTotal((int) page.getTotal());
        pageResponseResult.setData(page);
        // 返回
        return pageResponseResult;
    }

    /**
     * 删除素材
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult del(Integer id) {

        // 对id判空
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "参数失效");
        }
        // 查material表看是否存在该图片
        WmMaterial wmMaterial = wmMaterialMapper.selectById(id);
        if (wmMaterial == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "数据不存在");
        }
        // 先根据id查material-news表
        LambdaQueryWrapper<WmNewsMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmNewsMaterial::getMaterialId, id);
        List<WmNewsMaterial> wmNewsMaterials = wmNewsMaterialMapper.selectList(wrapper);
        int size = wmNewsMaterials.size();

        // 如果有返回值代表有数据, 返回删除失败
        if (size != 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文件删除失败,关联新闻条数: "+size);
        }

        // 没有返回值时可以删除
        if (size == 0) {
            wmMaterialMapper.deleteById(id);
            return ResponseResult.okResult(200, "操作成功");
        }

        return null;
    }

    /**
     * 取消收藏
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult cancel(Integer id) {
        //先查询id是否为空
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"非法输入");
        }
        //查询id是不是乱填的
        WmMaterial wmMaterial = wmMaterialMapper.selectById(id);
        if(wmMaterial==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"数据不存在");
        }
        //删除
        wmMaterialMapper.updateCollect(id);
        return ResponseResult.okResult(200,"取消收藏成功");
    }

    /**
     * 收藏
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult collect(Integer id) {

        //先查询id是否为空
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"非法输入");
        }
        //查询id是不是乱填的
        WmMaterial wmMaterial = wmMaterialMapper.selectById(id);
        if(wmMaterial==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"数据不存在");
        }
        //收藏
        WmMaterial wm = new WmMaterial();
        wm.setId(id);
        wm.setIsCollection((short)1);
        wmMaterialMapper.updateById(wm);

        return ResponseResult.okResult(200,"收藏成功");
    }
}
