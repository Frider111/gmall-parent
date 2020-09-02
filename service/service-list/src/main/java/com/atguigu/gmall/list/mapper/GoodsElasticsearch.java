package com.atguigu.gmall.list.mapper;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author Blue Grass
 * @date 2020/8/30 - 19:02
 */
public interface GoodsElasticsearch extends ElasticsearchRepository<Goods,Long> {



}
