package com.lc.finalexam.service;

import com.lc.finalexam.entity.Order;
import java.util.List;
import java.util.Map;

public interface OrderService {
    // 创建订单
    Order createOrder(Integer userId, List<Integer> cartItemIds, String recipientName, 
                     String recipientPhone, String shippingAddress, String note);
    
    // 根据ID获取订单
    Order getOrderById(Integer orderId);
    
    // 根据订单号获取订单
    Order getOrderByOrderNumber(String orderNumber);
    
    // 获取用户的所有订单
    List<Order> getOrdersByUser(Integer userId);
    
    // 获取用户的某状态订单
    List<Order> getOrdersByUserAndStatus(Integer userId, String status);
    
    // 更新订单状态
    Order updateOrderStatus(Integer orderId, String status);
    
    // 取消订单（仅限未支付订单）
    Order cancelOrder(Integer orderId);
    
    // 生成唯一订单号
    String generateOrderNumber();
    
    // 检查订单是否属于指定用户
    boolean isOrderBelongsToUser(Integer orderId, Integer userId);
    
    // 获取订单统计信息（总订单数、待支付数、已完成数等）
    Map<String, Long> getOrderStatistics(Integer userId);
    
    // === 管理员功能 ===
    
    // 获取所有订单
    List<Order> getAllOrders();
    
    // 获取所有特定状态的订单
    List<Order> getAllOrdersByStatus(String status);
    
    // 管理员获取订单统计信息
    Map<String, Long> getAdminOrderStatistics();
    
    // 管理员发货处理
    Order shipOrder(Integer orderId, String trackingNumber);
    
    // 管理员取消订单
    Order adminCancelOrder(Integer orderId, String reason);
} 