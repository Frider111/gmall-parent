package com.atguigu.gmall.payment.util;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Blue Grass
 * @date 2020/9/8 - 18:56
 */
@Configuration
@PropertySource(value = "classpath:alipay.properties")
public class AliPayConfig {

    @Value("${serverUrl}")
    private String serverUrl ;

    /**
     * 支付宝分配给开发者的应用ID
     */
    @Value("${appId}")
    private String appId ;

    /**
     * 私钥
     */
    @Value("${privateKey}")
    private String privateKey ;

    /**
     * 公钥
     */
    @Value("${publicKey}")
    private String publicKey ;

    public final static String format="json";

    public final static String charset="utf-8";
    /**
     * 	商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2
     */
    public final static String sign_type="RSA2";

    public static String return_payment_url ;

    public static String notify_payment_url ;

    public static String return_order_url ;

    @Value("${return_payment_url}")
    public void  setReturn_payment_url(String return_payment_url)
    {
        AliPayConfig.return_payment_url = return_payment_url ;
    }

    @Value("${notify_payment_url}")
    public void  setNotify_payment_url(String notify_payment_url)
    {
        AliPayConfig.notify_payment_url = notify_payment_url ;
    }


    @Value("${return_order_url}")
    public void  setReturn_order_url(String return_order_url)
    {
        AliPayConfig.return_order_url = return_order_url ;
    }

    // 封装 alipay 客户端
    @Bean
    public AlipayClient getAlipayClient(){
        return new DefaultAlipayClient(
                serverUrl,
                appId,
                privateKey,
                format,
                charset,
                publicKey,
                sign_type);
    }
}
