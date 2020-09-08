package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.gateway.util.LoginAuthUtil;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author Blue Grass
 * @date 2020/9/2 - 19:06
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    AntPathMatcher antPathMatcher = new AntPathMatcher() ;


    /**
     * 第一步 ， 让外界无法访问包含inner 跟 auth 路径的信息
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {



        // 获取请求头跟响应头数据
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 获取url跟path路径信息
        String url  = request.getURI().toString();
        String path = request.getPath().toString();
        // 判断路径包不包含inner ，包含的话，拒绝访问，不包含的话，继续下一个认证
        boolean innerMatch = antPathMatcher.match("/api/**/inner/**", path);
        // 不可以访问内部接口哦
        if (innerMatch){
            return out(response,ResultCodeEnum.PERMISSION) ;
        }
        boolean authrMatch = antPathMatcher.match("/api/**/auth/**", path);
        // 不可以访问内部接口哦
        if (authrMatch){

            String userId = LoginAuthUtil.getUserId(request);

            LoginAuthUtil.convertHeader(exchange, userId);

            if (StringUtils.isBlank(userId))
            {
                return out(response, ResultCodeEnum.LOGIN_AUTH) ;
            }
        }

        return chain.filter(exchange);
    }

    // 接口鉴权失败返回数据
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
// 返回用户没有权限登录
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
// 输入到页面
        return response.writeWith(Mono.just(wrap));
    }

}
