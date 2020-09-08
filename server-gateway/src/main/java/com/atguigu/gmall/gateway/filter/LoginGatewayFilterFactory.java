package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.gateway.util.LoginAuthUtil;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Blue Grass
 * @date 2020/9/4 - 14:31
 */
@Component
public class LoginGatewayFilterFactory extends AbstractGatewayFilterFactory {

    @Autowired
    UserFeignClient userFeignClient ;

    @Value("${authUrls.url}")
    String authUrls ;

    @Override
    public GatewayFilter apply(Object config) {
        return new GatewayFilter() {

            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();

                // 获取url跟path路径信息
                String url  = request.getURI().toString();
                String path = request.getPath().toString();
                // 获取用户 id 数据
                String userId = LoginAuthUtil.getUserId(request);

                String[] split = authUrls.split(",");

                for (String urlAuth : split) {
                    // 如果访问页面在白名单里面，就验证用户权限
                    if (path.indexOf(urlAuth)!=-1 &&  StringUtils.isBlank(userId))
                    {
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION,
                                "http://passport.gmall.com/login.html?originUrl="+url);
                        return response.setComplete();
                    }
                }
                String tempUserId = LoginAuthUtil.getTempUserId(request);

                LoginAuthUtil.convertHeader(exchange,userId,tempUserId);

                return chain.filter(exchange);
            }

        };
    }


}