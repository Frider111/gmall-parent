package com.atguigu.gmall.product.common.aspect;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.common.constant.RedisConst;
import com.atguigu.gmall.product.common.util.RandomUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Blue Grass
 * @date 2020/8/25 - 15:56
 */
@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    RedisTemplate redisTemplate;

    @Around("@annotation(com.atguigu.gmall.product.common.aspect.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {

//        环绕前置 【现在缓存查询数据，有数据直接返回数据】
        // 作为返回结果的数据，赋值等于空
        Object result = null;
        // 定义自旋次数

        int spinNum = 5 ;
        // 封装形参
//        String arg = getArg(joinPoint.getArgs());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GmallCache gmallAnno = signature.getMethod().getAnnotation(GmallCache.class);
        GmallParam gmallAnno1 = signature.getMethod().getAnnotation(GmallParam.class);
        // 预选封装 key 值的方法
        String redisId = redisId(signature.getMethod(), joinPoint.getArgs());
        // 封装 key 对象
        String key = getKey(gmallAnno, redisId);
        // 获取返回数据，如果有数据则直接返回，无数据则，去访问 db
        while (spinNum > 0 ) {

            result = cacheHit(signature, key);
            if (result == null) {
                String uuidLock = UUID.randomUUID().toString();
                String lockKey = gmallAnno.prefix() + redisId + RedisConst.SKULOCK_SUFFIX;
                Boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockKey,
                        uuidLock, 500, TimeUnit.SECONDS);
                // 如果是 true 说明已经获取到了锁，否则就没获取到锁
                if (isLock) {
                    result = joinPoint.proceed();
                    //  返回的数据，不管是 null 还是具体查询到了数据，都封装到 redis 里面 ，如果是null 设置时长【不超过5分钟】 ， 防止 缓存穿透
                    if (result == null) {
                        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result), 3, TimeUnit.MINUTES);
                    } else {
                        // 控制生成缓存的时间，不会让所有的缓存时间都是同一个时间，减轻缓存雪崩的可能
                        int hours = RandomUtil.numInt(5);
                        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result), 24l + hours, TimeUnit.HOURS);
                    }
                    luaDeleteLock(lockKey, uuidLock);
                    return result;
                }
                // 没获取到锁就自旋 ， 两种办法【限制执行次数，跟限制自旋时间】
                Thread.sleep(300);
            } else {
                return result;
            }
        }
        return result;
    }

    private Object cacheHit(MethodSignature signature, String key) {
        // 获取数据 redis 的 String数据类型 ： key，value 都是字符串
        String cache = (String) redisTemplate.opsForValue().get(key);
        // 从缓存中获取的字符串是否为空
        if (StringUtils.isNotBlank(cache)) {
            // 有数据 ，则将数据进行转化
            // 方法返回的数据类型
            Class returnType = signature.getReturnType();
            // 直接转化数据：
            return JSONObject.parseObject(cache, returnType);
        } else {
            return null;
        }
    }

    /**
     * 生成 key 的方法
     *
     * @param gmallAnno
     * @param redisId
     * @return
     */
    public String getKey(GmallCache gmallAnno, Object redisId) {
        String prefix = gmallAnno.prefix();
        String suffix = gmallAnno.suffix();
        String key = "product:"+prefix + redisId + suffix;
        return key;
    }

    /**
     * lua 脚本 删除锁
     *
     * @param lockKey
     * @param uuidLock
     */
    public void luaDeleteLock(String lockKey, String uuidLock) {
        // lua 脚本 删除锁
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        // 设置lua脚本返回的数据类型
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        // 设置lua脚本返回类型为Long
        redisScript.setResultType(Long.class);
        redisScript.setScriptText(script);
        redisTemplate.execute(redisScript, Arrays.asList(lockKey), uuidLock);
    }

    public String redisId(Method method,Object[] args){
        String redisId = "" ;
        boolean isAnnotation = false ;
        Map<Integer,Object> paramMap = new LinkedHashMap<>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        // 如果没有参数过来，直接把方法名字当做 redis 对应数据存入到 redis缓存
        if (parameterAnnotations == null || parameterAnnotations.length == 0 ){
            return method.getName() ;
        }
        // 遍历循环 参数注解数组
        for (int i = 0; i < parameterAnnotations.length; i++) {
           if (parameterAnnotations[i].length == 0){
               // 如果该参数没有注解 就给 map 加 null
               paramMap.put(i,null);
           }
           else {
               // 如果有注解记得在遍历注解数组
               for (Annotation annotation : parameterAnnotations[i]) {
                   // 如果注解可以强制转换为 GmallParam 就获取 GmallParam 的 value 值赋值给 paramMap ，并结束循环
                   if (annotation instanceof GmallParam){
                       GmallParam gmallParam = (GmallParam) annotation;
                       paramMap.put(i,gmallParam.value());
                       isAnnotation = true ;
                       break;
                   }else {
                       //  没找到就赋值 null
                       paramMap.put(i, null);
                   }
               }
           }
        }
        // 如果没有对应的参数注解 ，就返回第一个参数作为他的 redisId
        if (isAnnotation == false){
            return args[0]+"" ;
        }

        for (int i = 0; i < args.length; i++) {
            // 遍历参数值 ，如果注解值等于 null ，直接结束
            if (paramMap.get(i) == null){
                continue;
            }
            // 如果 注解值 等于 "" 说明有注解 ，但是没有注解值，就获取该注解标注字段的值当做 redisId
            else if ("".equals(paramMap.get(i)+"")) {
                redisId += args[i] + ":" ;
            }
            // 否则就把注解值当 redisId
            else {
                redisId += paramMap.get(i) + ":" ;
            }
        }
        return redisId.substring(0, redisId.length()-1);
    }

//    public String getArg(Object[] args){
//        String argStr = "";
//        if (args == null || args.length ==0 ){
//            return argStr ;
//        }
//        for (Object arg : args) {
//            argStr += arg ;
//        }
//        return argStr;
//    }

}
