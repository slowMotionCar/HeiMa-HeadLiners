package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/api/v1/material")
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;
    /**
     * 素材上传
     * @return
     */
    @PostMapping("/upload_picture")
    public ResponseResult upload(@RequestParam("multipartFile") MultipartFile file){
        return wmMaterialService.upload(file);
    }

    /**
     * 查询素材列表
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public PageResponseResult list(@RequestBody WmMaterialDto dto){
        return wmMaterialService.list(dto);
    }

    /**
     * 删除素材
     * @param id
     * @return
     */
    @GetMapping("/del_picture/{id}")
    public ResponseResult del(@PathVariable Integer id){
        return wmMaterialService.del(id);
    }

    /**
     * 取消收藏
     * @param id
     * @return
     */
    @GetMapping("/cancel_collect/{id}")
    public ResponseResult cancel(@PathVariable Integer id){
        log.info("取消收藏");
        return wmMaterialService.cancel(id);
    }

    /**
     * 收藏
     * @param id
     * @return
     */
    @GetMapping("/collect/{id}")
    public ResponseResult collect(@PathVariable Integer id){
        log.info("收藏");
        return wmMaterialService.collect(id);
    }
}
