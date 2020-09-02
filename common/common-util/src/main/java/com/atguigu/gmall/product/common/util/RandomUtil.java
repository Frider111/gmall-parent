package com.atguigu.gmall.product.common.util;

import java.util.Random;

/**
 * @author Blue Grass
 * @date 2020/8/25 - 18:54
 */
public class RandomUtil {

    private static Random random = new Random();

    public static int numInt(int n){

        return random.nextInt(n);
    }

    public static void main(String[] args) {
        int i = numInt(6);
        System.out.println("i = " + i);
        
    }
}
