package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.heima.api.feign.ApArticleFeignClient;
import com.heima.audit.baidu.BaiduImageScan;
import com.heima.audit.baidu.BaiduTextScan;
import com.heima.audit.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmAutoScanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WmAutoScanServiceImpl implements WmAutoScanService {

    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Autowired
    private BaiduTextScan baiduTextScan;

    @Autowired
    private BaiduImageScan baiduImageScan;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleFeignClient apArticleFeignClient;

    @Autowired
    private WmUserMapper wmUserMapper;

    @Autowired
    private WmChannelMapper wmChannelMapper;

    /**
     * 审核自媒体文章
     *主方法
     * @param newsId 自媒体文章id
     */
    @Override
    @Transactional
    public void autoScanWmNews(Integer newsId) {
        //TODO
    }

}
