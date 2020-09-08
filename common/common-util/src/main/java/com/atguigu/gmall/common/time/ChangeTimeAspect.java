package com.atguigu.gmall.common.time;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author Blue Grass
 * @date 2020/8/26 - 21:14
 */
@Aspect
@Component
public class ChangeTimeAspect {

    @Around("@annotation(com.atguigu.gmall.common.time.GmallChangeTime)")
    public Object changeTimeAdvice(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        GmallChangeTime annotation = method.getAnnotation(GmallChangeTime.class);

        Object proceed = joinPoint.proceed();


        long end = System.currentTimeMillis() - start;



        System.out.println(annotation.value() + end );

        return proceed ;
    }

}
