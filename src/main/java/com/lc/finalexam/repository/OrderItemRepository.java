package com.lc.finalexam.repository;

import com.lc.finalexam.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    // 根据订单ID查找订单项
    List<OrderItem> findByOrder_Id(Integer orderId);
} 