package com.leyou.cart.service;

import com.leyou.cart.pojo.Cart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {
    void addCart(Cart cart);

    List<Cart> queryCartList();

    void updateCartNum(Long skuId, Integer num);

    void deleteCart(Long skuId);
}
