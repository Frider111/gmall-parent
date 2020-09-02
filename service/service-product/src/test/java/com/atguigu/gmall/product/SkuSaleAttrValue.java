package com.atguigu.gmall.product;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/8/23 - 17:48
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SkuSaleAttrValue {

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper ;

    @Test
    public void test1() {
//        List<Map<String, Object>> skuValueIdsMap = skuSaleAttrValueMapper.getSkuValueIdsMap(1l);

//       skuValueIdsMap.forEach( (key,value) -> {
//           System.out.println("skuValueIdsMap = " + skuValueIdsMap);
//       });

//        System.out.println("skuValueIdsMap = " + skuValueIdsMap);
//
//        for (int i = 0; i < skuValueIdsMap.size(); i++) {
//            skuValueIdsMap.get(i).forEach((key, value) -> {
//                System.out.println("1111 = " + 1111);
//                System.out.print("key = " + key);
//                System.out.println("value = " + value);
//            });
//        }


        List<SpuSaleAttr> spuSaleAttrListCheckBySku1 = spuSaleAttrMapper.getSpuSaleAttrListCheckBySku1(1l, 1l);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrListCheckBySku1) {
            System.out.println("spuSaleAttr = " + spuSaleAttr);
            System.out.println("spuSaleAttr = " + spuSaleAttr.getId());
        }


    }

    @Test
    public void test2(){
        List<BaseAttrInfo> attrList = baseAttrInfoMapper.getAttrList(1l);
        attrList.stream().forEach(System.out::println);

    }


}
