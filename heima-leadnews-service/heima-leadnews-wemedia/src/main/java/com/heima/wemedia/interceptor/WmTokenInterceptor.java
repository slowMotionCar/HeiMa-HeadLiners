package com.heima.wemedia.interceptor;

import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.AppJwtUtil;
import com.heima.utils.threadlocal.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmUserMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义的拦截器
 */
@Slf4j
@Component
public class WmTokenInterceptor implements HandlerInterceptor {


    /**
     * 执行目标方法之前执行的过滤器方法
     *
     * @param request
     * @param response
     * @param handler
     * @return true表示放行，false表示拦截
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        log.info("wm正在拦截{}", requestURI);
        // 获取token
        String token = request.getHeader("token");

        //如果是空则返回
        if(StringUtils.isEmpty(token)){
            return false;
        }
        // 解析token
        Claims claimsBody = AppJwtUtil.getClaimsBody(token);
        // 取出id

        Integer id = claimsBody.get("id",Integer.class);
        System.out.println(id);
        WmUser wmUser = new WmUser();
        wmUser.setId(id);

        // ThreadLocal
        WmThreadLocalUtil.setUser(wmUser);

        // 4.放行
        return true;
    }

    /**
     * 完成所有的请求之后，执行的方法
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 释放资源

        WmThreadLocalUtil.clear();
    }
}
