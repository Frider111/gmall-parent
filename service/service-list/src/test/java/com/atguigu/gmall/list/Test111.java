package com.atguigu.gmall.list;

import com.atguigu.gmall.list.mapper.GoodsElasticsearch;
import com.atguigu.gmall.model.list.Goods;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Blue Grass
 * @date 2020/8/31 - 8:29
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Test111 {

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate ;

    @Autowired
    RestHighLevelClient restHighLevelClient ;

    @Autowired
    GoodsElasticsearch elasticsearch ;

    @Autowired
    RedisTemplate redisTemplate ;



    @Test
    public void test1(){

        boolean index = elasticsearchRestTemplate.createIndex(Goods.class);
        System.out.println("index = " + index);
        boolean b = elasticsearchRestTemplate.putMapping(Goods.class);
        System.out.println("b = " + b);


    }


    @Test
    public void test2(){

        elasticsearchRestTemplate.deleteIndex(Goods.class) ;
        Goods goods = new Goods() ;
        elasticsearch.index(goods) ;


    }


    @Test
    public void test3(){

//        SearchSourceBuilder
        Double aDouble = redisTemplate.opsForZSet().incrementScore("111", "22", 0);

        System.out.println("aDouble = " + aDouble);


    }


}
