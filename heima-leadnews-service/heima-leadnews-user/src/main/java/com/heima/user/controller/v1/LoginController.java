package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/login")
@Api(value = "登录用户管理")
@Slf4j
public class LoginController {

    @Autowired
    private ApUserService apUserService;
    /**
     * 登录接口
     * @param dto
     * @return
     */
    @PostMapping("/login_auth")
    @ApiOperation(value = "登录接口")
    public ResponseResult login(@RequestBody LoginDto dto){
        log.info("登陆了{}",dto);
        return apUserService.login(dto);
    }

    @GetMapping("")
    @ApiOperation(value = "登录接口")
    public void no(){
        log.info("hello");
        return;
    }
}
