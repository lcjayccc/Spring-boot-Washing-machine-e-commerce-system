package com.lc.finalexam.repository;

import com.lc.finalexam.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    // 根据用户ID查找订单
    List<Order> findByUser_IdOrderByCreateTimeDesc(Integer userId);
    
    // 根据用户ID和订单状态查找订单
    List<Order> findByUser_IdAndStatusOrderByCreateTimeDesc(Integer userId, String status);
    
    // 根据订单编号查找订单
    Order findByOrderNumber(String orderNumber);
    
    // 根据订单状态查找订单
    List<Order> findByStatus(String status);
    
    // 按创建时间降序查找所有订单
    List<Order> findAllByOrderByCreateTimeDesc();
}