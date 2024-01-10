package com.heima.article.controller.v1;



import com.baomidou.mybatisplus.extension.api.R;
import com.heima.article.service.EsService;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @Description ElasticSearch
 * @Author Zhilin
 * @Date 2023-11-07
 */
@RestController
@RequestMapping("/api/v1/article")
@Slf4j
public class ElasticSearch {

    @Autowired
    private EsService esService;

    //同步所有es消息
    @PostMapping("/esAll")
    public ResponseResult load() throws IOException {

        esService.setAll();

        return ResponseResult.okResult(200,"同步了,不信自己看");
    }


}
