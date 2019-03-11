package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX="cart:userId:";

    @Override
    public void addCart(Cart cart) {
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        String hashKey = cart.getSkuId().toString();

        //1.判读当前商品是否存在
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        if (operation.hasKey(hashKey)) {
            //存在，增加数量
            String json = operation.get(hashKey).toString();
            Cart cacheCart = JsonUtils.toBean(json, Cart.class);
            cacheCart.setNum(cacheCart.getNum()+cart.getNum());
            operation.put(hashKey,JsonUtils.toString(cacheCart));

        }else {
            //不存在，新增
            operation.put(hashKey,JsonUtils.toString(cart));
        }
    }


    @Override
    public List<Cart> queryCartList() {
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();

        if (!redisTemplate.hasKey(key)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }

        //获取当前用户下的所有购物车商品列表
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        List<Cart> carts = operation.values().stream().map(o -> JsonUtils.toBean(o.toString(), Cart.class)).collect(Collectors.toList());

        return carts;
    }

    @Override
    public void updateCartNum(Long skuId, Integer num) {
        //获取用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();

        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);

        if (!operation.hasKey(skuId.toString()))
        {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }

        Cart cart = JsonUtils.toBean(operation.get(skuId.toString()).toString(), Cart.class);
        cart.setNum(num);

        operation.put(skuId.toString(),JsonUtils.toString(cart));
    }


    @Override
    public void deleteCart(Long skuId) {
        //获取用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();

        redisTemplate.opsForHash().delete(key,skuId.toString());
    }
}
