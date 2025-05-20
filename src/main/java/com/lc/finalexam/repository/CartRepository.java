package com.lc.finalexam.repository;

import com.lc.finalexam.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    // 查找某个用户的购物车
    Cart findByUser_Id(Integer userId);
}
