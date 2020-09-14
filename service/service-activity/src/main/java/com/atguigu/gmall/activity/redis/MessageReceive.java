package com.atguigu.gmall.activity.redis;

import com.atguigu.gmall.activity.util.CacheHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Blue Grass
 * @date 2020/9/13 - 14:06
 */
@Component
public class MessageReceive {

    public void receiveMessage(String mes)
    {
        System.out.println(mes);
        if (StringUtils.isNotBlank(mes))
        {
            mes = mes.replaceAll("\"","");
            String[] split = mes.split(":");
            for (String s : split) {
                CacheHelper.put(split[0],split[1]);
            }
        }
    }

}
