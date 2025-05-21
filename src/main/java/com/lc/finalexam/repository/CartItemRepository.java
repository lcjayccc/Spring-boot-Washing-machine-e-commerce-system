package com.lc.finalexam.repository;

import com.lc.finalexam.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    // 根据购物车ID和商品ID查找购物车项
    CartItem findByCart_IdAndProduct_Id(Integer cartId, Integer productId);
    
    // 根据购物车ID查找所有购物车项
    Iterable<CartItem> findByCart_Id(Integer cartId);
    
    // 根据ID列表查找购物车项
    List<CartItem> findByIdIn(List<Integer> ids);
} 