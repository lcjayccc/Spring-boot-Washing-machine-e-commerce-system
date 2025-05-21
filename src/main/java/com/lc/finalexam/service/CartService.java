package com.lc.finalexam.service;

import com.lc.finalexam.entity.Cart;
import com.lc.finalexam.entity.CartItem;

import java.util.List;
import java.util.Map;

public interface CartService {
    /**
     * 获取用户的购物车，如果不存在则创建
     */
    Cart getOrCreateCart(Integer userId);
    
    /**
     * 添加商品到购物车
     */
    CartItem addProductToCart(Integer userId, Integer productId, Integer quantity);
    
    /**
     * 从购物车中移除商品
     */
    void removeProductFromCart(Integer userId, Integer productId);
    
    /**
     * 移除购物车中的多个商品
     */
    void removeProductsFromCart(Integer userId, List<Integer> productIds);
    
    /**
     * 更新购物车中商品的数量
     */
    CartItem updateProductQuantity(Integer userId, Integer productId, Integer quantity);
    
    /**
     * 获取购物车中的所有项
     */
    List<CartItem> getCartItems(Integer userId);
    
    /**
     * 计算购物车总价
     */
    double calculateCartTotal(Integer userId);
    
    /**
     * 计算多个商品的总价
     */
    Map<String, Object> calculateSelectedItems(Integer userId, List<Integer> itemIds);
    
    /**
     * 清空购物车
     */
    void clearCart(Integer userId);
} 