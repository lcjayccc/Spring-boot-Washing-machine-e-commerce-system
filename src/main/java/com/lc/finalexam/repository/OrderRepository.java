package com.lc.finalexam.repository;

import com.lc.finalexam.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    // 查找某个用户的所有订单
    List<Order> findByUser_Id(Integer userId);
}