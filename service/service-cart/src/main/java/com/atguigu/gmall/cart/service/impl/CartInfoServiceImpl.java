package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Blue Grass
 * @date 2020/9/5 - 19:17
 */
@Service
public class CartInfoServiceImpl extends ServiceImpl<CartInfoMapper, CartInfo> implements CartInfoService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    RedisTemplate redisTemplate;


    public CartInfo getCartInfo(Long skuId, Integer skuNum, String userId) {

        CartInfo cartInfo = new CartInfo();

        BigDecimal price = productFeignClient.getSkuPrice(skuId);

        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        cartInfo.setSkuId(skuId)
                .setSkuName(skuInfo.getSkuName())
                .setSkuPrice(price)
                .setCartPrice(price.multiply(new BigDecimal(skuNum)))
                .setImgUrl(skuInfo.getSkuDefaultImg())
                .setUserId(userId)
                .setSkuNum(skuNum);
        return cartInfo;
    }


    /**
     * 第一步 ：根据 sjuId 跟 userID 在数据库中查询数据，如果等于空 ，就添加数据，并把数据同步在缓存当中
     * 如果有数据，就更新 skuNum 数据 ，并且一起更新 redis 缓存数据
     *
     * @param skuId
     * @param skuNum
     * @param userId
     */
    @Override
    @Transactional
    public void addToCart(Long skuId, Integer skuNum, String userId) {

        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();

        cartInfoQueryWrapper.eq("sku_id", skuId)
                .eq("user_id", userId);

        CartInfo cartInfo1 = this.getOne(cartInfoQueryWrapper);

        // 等于null的话 说明，数据库没有数据，需要添加数据
        if (cartInfo1 == null) {
            cartInfo1 = getCartInfo(skuId, skuNum, userId);
            this.save(cartInfo1);
        } else {
            cartInfo1.setSkuNum(cartInfo1.getSkuNum() + skuNum);
            BigDecimal price = productFeignClient.getSkuPrice(skuId);
            cartInfo1.setSkuPrice(price);
            this.updateById(cartInfo1);
        }
        // redis 同步数据
        redisTemplate.boundHashOps(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX)
                .put(skuId + "", cartInfo1);
    }

    @Override
    @Transactional
    public List<CartInfo> cartList(String userId, String userTempId) {

        List<CartInfo> values = new ArrayList();
        if (StringUtils.isBlank(userId)) {
            values = redisTemplate.boundHashOps(
                    RedisConst.USER_KEY_PREFIX + userTempId + RedisConst.USER_CART_KEY_SUFFIX).values();

            // 从缓存获取数据，并且更新最新的价格数据
            values = values.stream().map((value) -> {
                BigDecimal price = productFeignClient.getSkuPrice(value.getSkuId());
                value.setSkuPrice(price);
                return value;
            }).collect(Collectors.toList());
        } else {
            QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();

            cartInfoQueryWrapper.eq("user_id", userTempId).or().eq("user_id", userId);

            List<CartInfo> tempIds = this.list(cartInfoQueryWrapper);

            Map<Long, CartInfo> cartInfoMap = new HashMap<>();

            tempIds.stream().forEach((cartInfo) -> {
                // 存在修改数量,两者相加

                if (cartInfo.getUserId().equals(userTempId)) {
                    // 删除redis中的临时数据
                    redisTemplate.boundHashOps(
                            RedisConst.USER_KEY_PREFIX + userTempId + RedisConst.USER_CART_KEY_SUFFIX).delete(cartInfo.getSkuId() + "");
                    // 删除 数据中的临时数据
                    this.removeById(cartInfo.getId());
                    // 设置当前对象数据等于 userId
                    cartInfo.setUserId(userId);
                }

                if (cartInfoMap.containsKey(cartInfo.getSkuId())) {
                    // 存在就修改数量，两个用户之间数据相加
                    CartInfo cartInfo1 = cartInfoMap.get(cartInfo.getSkuId());
                    cartInfo1.setSkuNum(cartInfo1.getSkuNum() + cartInfo.getSkuNum());
                } else {
                    // 不存在直接添加
                    cartInfoMap.put(cartInfo.getSkuId(), cartInfo);
                }
            });
            // 更新数据

            for (Map.Entry<Long, CartInfo> longCartInfoEntry : cartInfoMap.entrySet()) {


                String skuId = longCartInfoEntry.getKey().toString();
                CartInfo cartInfo = longCartInfoEntry.getValue();

                // 更新数据库数据
                QueryWrapper<CartInfo> cartInfoQueryWrapper1 = new QueryWrapper<>();

                cartInfoQueryWrapper1.eq("sku_id", skuId).eq("user_id", cartInfo.getUserId());

                this.update(cartInfo, cartInfoQueryWrapper1);
                // 重新获取价格数据
                BigDecimal price = productFeignClient.getSkuPrice(cartInfo.getSkuId());
                cartInfo.setSkuPrice(price);
                // 数据重新添加到缓存中
                redisTemplate.boundHashOps(
                        RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX)
                        .put(cartInfo.getSkuId() + "", cartInfo);

            }

            // 从缓存中拿取数据
            values = redisTemplate.boundHashOps(
                    RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX).values();
        }
        // 返回结果
        return values;
    }

    @Override
    public void checkCart(Long skuId, Integer isChecked, String userId) {


        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();

        cartInfoQueryWrapper.eq("sku_id", skuId)
                .eq("user_id", userId);

        CartInfo cartInfo1 = this.getOne(cartInfoQueryWrapper);
        cartInfo1.setIsChecked(isChecked);
        this.updateById(cartInfo1);
        BigDecimal price = productFeignClient.getSkuPrice(skuId);
        cartInfo1.setSkuPrice(price);
        redisTemplate.boundHashOps(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX)
                .put(skuId + "", cartInfo1);
    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();

        cartInfoQueryWrapper.eq("sku_id", skuId)
                .eq("user_id", userId);

        this.remove(cartInfoQueryWrapper);
        redisTemplate.boundHashOps(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX)
                .delete(skuId + "");
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {

        List<CartInfo> values = new ArrayList<>();

        if (StringUtils.isBlank(userId)) {
            return null;
        }

        values = redisTemplate.boundHashOps(
                RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX).values();

        if (values == null | values.isEmpty()) {
            QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
            cartInfoQueryWrapper.eq("user_id", userId);
            List<CartInfo> list = this.list(cartInfoQueryWrapper);

            values = list.stream().map(cartInfo -> {

                BigDecimal price = productFeignClient.getSkuPrice(cartInfo.getSkuId());
                cartInfo.setSkuPrice(price);

                redisTemplate.boundHashOps(
                        RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX)
                        .put(cartInfo.getSkuId() + "", cartInfo);

                return cartInfo;
            }).collect(Collectors.toList());
        }

        values = values.stream().filter((value) -> {
            return value.getIsChecked()==1?true:false ;
        }).collect(Collectors.toList());


        return values;
    }
}

