package com.azaz.filter;

import com.azaz.constant.UserConstant;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * 登录过滤器
 * @author shigc
 */
@Order(0)
@Component
@Log4j2
public class LoginFilter implements GlobalFilter {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 过滤器
     * @param exchange 请求上下文
     * @param chain 过滤器链
     * @return 过滤结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RequestPath path = exchange.getRequest().getPath();
        //1.获取request和response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 判断是否是退出登录
        if (request.getURI().getPath().contains("/logout")) {
            String token = request.getHeaders().getFirst("token");
            //删除redis中的token
            stringRedisTemplate.delete(UserConstant.REDIS_LOGIN_TOKEN + token);
            return exchange.getResponse().setComplete();
        }
        //2.判断是否是登录
        if (request.getURI().getPath().contains("/login")) {
            //放行
            return chain.filter(exchange);
        }
        // 如果是其他请求，判断是否已经登录
        String token = request.getHeaders().getFirst("token");
        //跟据token从redis中拿到用户id
        String userId = stringRedisTemplate.opsForValue().get(UserConstant.REDIS_LOGIN_TOKEN + token);
        //判断是否登录
        if (userId != null) {
            try {
            // 刷新token有效期
            stringRedisTemplate.opsForValue().set(UserConstant.REDIS_LOGIN_TOKEN + token, userId,
                    UserConstant.LOGIN_USER_TTL, java.util.concurrent.TimeUnit.SECONDS);

                //存储header中
                ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
                    httpHeaders.add("userId", userId);
                }).build();
                //重置请求
                exchange.mutate().request(serverHttpRequest);
                //6.放行
                return chain.filter(exchange);
            }catch (Exception e){
               log.error(e);
            }
        }
        // 如果未登录，返回错误信息
        log.info("拦截到未登录请求:{}", path.toString());
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
