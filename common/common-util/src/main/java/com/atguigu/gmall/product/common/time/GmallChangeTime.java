package com.atguigu.gmall.product.common.time;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Blue Grass
 * @date 2020/8/26 - 21:12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GmallChangeTime {

    String value() default "这个方法的执行时间是：" ;

}
