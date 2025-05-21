package com.lc.finalexam.service;

import com.lc.finalexam.entity.Payment;
import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {
    // 为订单创建支付记录
    Payment createPayment(Integer orderId, String paymentMethod);
    
    // 处理支付（模拟支付流程）
    Map<String, Object> processPayment(Integer paymentId, BigDecimal amount);
    
    // 处理支付回调（模拟支付平台回调）
    boolean handlePaymentCallback(String paymentNumber, String transactionId, String status);
    
    // 根据ID获取支付记录
    Payment getPaymentById(Integer paymentId);
    
    // 根据订单ID获取支付记录
    Payment getPaymentByOrderId(Integer orderId);
    
    // 根据支付流水号获取支付记录
    Payment getPaymentByPaymentNumber(String paymentNumber);
    
    // 更新支付状态
    Payment updatePaymentStatus(Integer paymentId, String status);
    
    // 生成唯一支付流水号
    String generatePaymentNumber();
} 