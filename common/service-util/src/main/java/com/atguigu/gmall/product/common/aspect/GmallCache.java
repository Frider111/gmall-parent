package com.atguigu.gmall.product.common.aspect;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Blue Grass
 * @date 2020/8/25 - 15:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
public @interface GmallCache {

    String prefix() default "cache:" ;

    String suffix() default ":info" ;

}
