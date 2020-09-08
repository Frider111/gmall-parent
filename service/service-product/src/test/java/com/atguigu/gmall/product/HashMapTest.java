package com.atguigu.gmall.product;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/3 - 8:25
 */
public class HashMapTest {

    @Test
    public void test(){

        HashMap map = new HashMap();
        for (int i = 0; i < 100; i++) {
            map.put(i, i);
        }
    }

}
