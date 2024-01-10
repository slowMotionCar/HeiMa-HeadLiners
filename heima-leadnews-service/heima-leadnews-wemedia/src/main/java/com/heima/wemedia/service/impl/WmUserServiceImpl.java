package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.AppJwtUtil;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class WmUserServiceImpl extends ServiceImpl<WmUserMapper, WmUser> implements WmUserService {

    @Resource
    private WmUserMapper wmUserMapper;

    /**
     * 登陆
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(WmLoginDto dto) {

        // dto 判空
        if (StringUtils.isEmpty(dto.getName())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        // 不为空/
        // 判断用户是否存在
        // 获取用户
        LambdaQueryWrapper<WmUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmUser::getName, dto.getName());
        WmUser wmUser = wmUserMapper.selectOne(wrapper);

        if (wmUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NON_EXISTS);
        }

        // 判断用户密码是否正确
        String salt = wmUser.getSalt();
        String passwordUser = wmUser.getPassword();
        String saltyPassword = dto.getPassword() + salt;
        String md5DigestAsHex = DigestUtils.md5DigestAsHex(saltyPassword.getBytes());
        if (!(passwordUser.equals(md5DigestAsHex))) {
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }

        // 用户正常, 给予token
        String token = AppJwtUtil.getToken(wmUser.getId().longValue());
        Map<String, Object> mapReturn = new HashMap<>();
        // 隐藏返回密码和salt的对象
        wmUser.setPassword("");
        wmUser.setSalt("");
        mapReturn.put("user", wmUser);
        mapReturn.put("token", token);

        return ResponseResult.okResult(mapReturn);

    }
}