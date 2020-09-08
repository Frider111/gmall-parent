package com.atguigu.gmall.gateway.util;

import com.atguigu.gmall.common.util.SpringUtil;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/9/4 - 18:20
 */
public class LoginAuthUtil {


    public static String getToken(ServerHttpRequest request,String cookieName){


        MultiValueMap<String, HttpCookie> cookies = request.getCookies();

        if ( cookies==null || cookies.isEmpty() )
        {
            return null ;
        }
        String name = cookies.get(cookieName).get(0).getValue();

        return name;
    }

    public static String getUserId(ServerHttpRequest request){

        UserFeignClient userFeignClient = SpringUtil.getBean(UserFeignClient.class);

        String token = getToken(request,"token");

        if (StringUtils.isEmpty(token))
        {
            List<String> list =  request.getHeaders().get("token") ;
            if (list==null || list.isEmpty())
            {
                return null ;
            }
            token = list.get(0).toString() ;

        }
        String userId = userFeignClient.verify(token);

        return userId ;
    }


    public static String getTempUserId(ServerHttpRequest request){

        String tempUserId = "";
        tempUserId = getToken(request,"userTempId");
        return  tempUserId;
    }

    // 重新编译请求头
    public static void convertHeader(ServerWebExchange exchange,String userId){


        ServerHttpRequest request = exchange.getRequest();
        request.mutate().header("userId", userId).build();
        exchange.mutate().request(request).build();

    }

    // 重新编译请求头
    public static void convertHeader(ServerWebExchange exchange,String userId,String tempUserId){

        ServerHttpRequest request = exchange.getRequest();

        if (!StringUtils.isEmpty(userId))
        {
            request.mutate().header("userId", userId).build();
        }
        if (!StringUtils.isEmpty(tempUserId))
        {
            request.mutate().header("userTempId", tempUserId).build();
        }
        exchange.mutate().request(request).build();

    }

}
