package com.heima.gateway.filter;

import com.heima.gateway.utils.AppJwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;

/*
全局过滤器，校验token是否有效
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {


    /**
     * 执行过滤的方法
     *
     * @param exchange 交换机，可以获取请求和响应对象
     * @param chain    过滤器链
     * @return
     */

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 获取请求和响应
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        System.out.println("hello,我在这里");
        // 登陆界面不检查
        // localhost:51601/api/v1/login/login_auth
        URI requestURI = request.getURI();
        // 获取请求路径  /api/v1/login/login_auth
        String path = requestURI.getPath();
        if (path.contains("/login/login_auth")) {
            return chain.filter(exchange);
        }
        // 获取token
        HttpHeaders headers = request.getHeaders();
        String token = headers.getFirst("token");

        // 检查token是否为空
        if (token == null) {
            response.setStatusCode(HttpStatus.BAD_GATEWAY);
            // Mono<Void>类型通常用于表示不需要返回具体结果的情况，比如仅仅需要通知完成状态的操作。
            // 在代码中，voidMono变量用于接收response.setComplete()方法返回的Mono<Void>对象。
            // 然后，使用return voidMono语句将这个Mono<Void>对象返回，以结束当前方法并将其作为结果返回给调用者
            Mono<Void> voidMono = response.setComplete();
            return voidMono;
        }
        // 检查token是否正常, 不正常返回
        Claims claimsBody = AppJwtUtil.getClaimsBody(token);
        int i = AppJwtUtil.verifyToken(claimsBody);
        if (i == 1 || i == 2) {
            response.setStatusCode(HttpStatus.BAD_GATEWAY);
            Mono<Void> voidMono = response.setComplete();
            return voidMono;
        }

        return chain.filter(exchange);

    }

    /**
     * 过滤的执行的优先级，返回值越小优先级越高
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
