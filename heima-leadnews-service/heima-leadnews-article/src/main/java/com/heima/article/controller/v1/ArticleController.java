package com.heima.article.controller.v1;

import com.heima.article.service.ArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.constants.ArticleConstants;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/article")
@Slf4j
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 加载首页
     * @param dto
     * @return
     */
    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto dto){
        log.info("加载首页",dto);
        return articleService.load(dto, (short) 3);
    }
    /**
     * 加载更多，上拉
     * @param dto
     * @return
     */
    @PostMapping("/loadmore")
    public ResponseResult loadmore(@RequestBody ArticleHomeDto dto){
        return articleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }
    /**
     * 加载更新，下拉
     * @param dto
     * @return
     */
    @PostMapping("/loadnew")
    public ResponseResult loadnew(@RequestBody ArticleHomeDto dto){
        return articleService.load(dto, ArticleConstants.LOADTYPE_LOAD_NEW);
    }

    /**
     * 保存三剑客
     * @return
     */
    @PostMapping("/save")
    public ResponseResult save(@RequestBody ArticleDto dto){
        log.info("正在添加");
        return articleService.save(dto);
    }
}
