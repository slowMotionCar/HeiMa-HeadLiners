package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojo.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import feign.template.QueryTemplate;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.BeanUtils;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    @Resource
    private ApUserMapper apUserMapper;

    /**
     * 登录接口
     * 二合一，用户登录也是有游客登录
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto dto) {

        // 封装回执
        Map<String, Object> mapReturn = new HashMap<>();
        ApUser apUserHit = new ApUser();
        String token = "";
        // 输入为空==游客
        if (Objects.equals(dto.getPhone(), "") && Objects.equals(dto.getPassword(), "")) {
            apUserHit = null;
            token = AppJwtUtil.getToken(0L);

        } else {
            // 输入不为空
            // 用户名为空
            LambdaQueryWrapper<ApUser> wrapper = new LambdaQueryWrapper();
            wrapper.eq(ApUser::getPhone, dto.getPhone());
            List<ApUser> apUsers = apUserMapper.selectList(wrapper);
            if (CollectionUtils.isEmpty(apUsers)) {
                return ResponseResult.setAppHttpCodeEnum(AppHttpCodeEnum.NON_EXISTS);
            }
            apUserHit = apUsers.get(0);
            // 密码错误
            // 生成盐
            String salt = apUserHit.getSalt();
            String pwdEnteredWithSalt = dto.getPassword() + salt;
            String md5DigestAsHex = DigestUtils.md5DigestAsHex(pwdEnteredWithSalt.getBytes());
            if (!(apUserHit.getPassword().equals(md5DigestAsHex))) {
                return ResponseResult.setAppHttpCodeEnum(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            // 剩下都是客户
            // 生成token
            token = AppJwtUtil.getToken(apUserHit.getId().longValue());

        }
        mapReturn.put("user", apUserHit);
        mapReturn.put("token", token);
        return new ResponseResult<>(200, "登陆成功", mapReturn);
    }

    /**
     * 根据用户id获取用户实体
     *
     * @param userId
     * @return
     */
    @Override
    public ResponseResult<ApUser> findUserById(Integer userId) {
        ApUser apUser = apUserMapper.selectById(userId);
        return ResponseResult.okResult(apUser);
    }
}
