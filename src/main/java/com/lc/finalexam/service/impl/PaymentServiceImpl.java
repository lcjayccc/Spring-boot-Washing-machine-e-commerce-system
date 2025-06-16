package com.lc.finalexam.service.impl;

import com.lc.finalexam.entity.Order;
import com.lc.finalexam.entity.Payment;
import com.lc.finalexam.exception.OrderException;
import com.lc.finalexam.exception.PaymentException;
import com.lc.finalexam.repository.OrderRepository;
import com.lc.finalexam.repository.PaymentRepository;
import com.lc.finalexam.service.OrderService;
import com.lc.finalexam.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class PaymentServiceImpl implements PaymentService {
    
    // 使用静态Random实例，提高随机性
    private static final Random random = new Random();

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Override
    @Transactional
    public Payment createPayment(Integer orderId, String paymentMethod) {
        // 检查支付方式是否支持
        validatePaymentMethod(paymentMethod);
        
        // 获取订单
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(orderId));
        
        // 检查订单状态
        if (!Order.STATUS_PENDING.equals(order.getStatus())) {
            throw new PaymentException.PaymentCreationException("只有待支付状态的订单可以创建支付记录");
        }
        
        // 检查是否已经存在支付记录
        Optional<Payment> existingPayment = paymentRepository.findByOrder_Id(orderId);
        if (existingPayment.isPresent()) {
            // 如果有未完成的支付，返回该支付记录
            Payment payment = existingPayment.get();
            if (Payment.STATUS_PENDING.equals(payment.getStatus())) {
                return payment;
            } else {
                throw new PaymentException.PaymentCreationException("订单已有支付记录");
            }
        }
        
        // 创建新的支付记录
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentNumber(generatePaymentNumber());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(Payment.STATUS_PENDING);
        payment.setCreateTime(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Map<String, Object> processPayment(Integer paymentId, BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取支付记录
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(paymentId));
        
        // 检查支付状态
        if (!Payment.STATUS_PENDING.equals(payment.getStatus())) {
            throw new PaymentException.InvalidPaymentStatusException("支付记录状态不是待支付");
        }
        
        // 检查支付金额是否匹配
        if (payment.getAmount().compareTo(amount) != 0) {
            throw new PaymentException.PaymentAmountMismatchException(
                    payment.getAmount().doubleValue(), amount.doubleValue());
        }
        
        try {
            // 模拟支付处理，有20%的概率支付成功
            boolean isSuccess = random.nextInt(100) < 20;
            
            if (isSuccess) {
                // 支付成功
                payment.setStatus(Payment.STATUS_SUCCESS);
                payment.setTransactionId(generateTransactionId());
                payment.setPayTime(LocalDateTime.now());
                
                // 更新订单状态为已支付
                Order order = payment.getOrder();
                orderService.updateOrderStatus(order.getId(), Order.STATUS_PAID);
                
                result.put("success", true);
                result.put("message", "支付成功");
                result.put("paymentId", payment.getId());
                result.put("transactionId", payment.getTransactionId());
            } else {
                // 支付失败
                payment.setStatus(Payment.STATUS_FAILED);
                
                result.put("success", false);
                result.put("message", "支付失败，请重试");
                result.put("paymentId", payment.getId());
            }
            
            paymentRepository.save(payment);
            return result;
        } catch (Exception e) {
            throw new PaymentException.PaymentProcessingException("处理支付时发生错误: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean handlePaymentCallback(String paymentNumber, String transactionId, String status) {
        // 获取支付记录
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new PaymentException("支付流水号不存在: " + paymentNumber));
        
        // 已经处理过的支付不再处理
        if (!Payment.STATUS_PENDING.equals(payment.getStatus())) {
            return false;
        }
        
        if (Payment.STATUS_SUCCESS.equals(status)) {
            // 支付成功
            payment.setStatus(Payment.STATUS_SUCCESS);
            payment.setTransactionId(transactionId);
            payment.setPayTime(LocalDateTime.now());
            
            // 更新订单状态
            Order order = payment.getOrder();
            orderService.updateOrderStatus(order.getId(), Order.STATUS_PAID);
        } else {
            // 支付失败
            payment.setStatus(Payment.STATUS_FAILED);
            // 不需要更新订单状态，订单仍保持待支付状态
        }
        
        paymentRepository.save(payment);
        return true;
    }

    @Override
    public Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(paymentId));
    }

    @Override
    public Payment getPaymentByOrderId(Integer orderId) {
        return paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new PaymentException("订单没有支付记录: " + orderId));
    }

    @Override
    public Payment getPaymentByPaymentNumber(String paymentNumber) {
        return paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new PaymentException("支付流水号不存在: " + paymentNumber));
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(Integer paymentId, String status) {
        Payment payment = getPaymentById(paymentId);
        
        // 检查状态转换是否合法
        validateStatusTransition(payment.getStatus(), status);
        
        payment.setStatus(status);
        
        // 如果更新为支付成功，设置支付时间和更新订单状态
        if (Payment.STATUS_SUCCESS.equals(status)) {
            payment.setPayTime(LocalDateTime.now());
            
            // 更新订单状态
            Order order = payment.getOrder();
            orderService.updateOrderStatus(order.getId(), Order.STATUS_PAID);
        }
        
        return paymentRepository.save(payment);
    }

    @Override
    public String generatePaymentNumber() {
        // 生成格式为：P+年月日时分秒+6位随机数的支付流水号
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%06d", random.nextInt(1000000));
        return "P" + timestamp + randomStr;
    }
    
    // 生成模拟的第三方交易ID
    private String generateTransactionId() {
        // 生成格式为：T+年月日+10位随机数的交易ID
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%010d", random.nextInt(1000000000));
        return "T" + date + randomStr;
    }
    
    // 检查支付方式是否支持
    private void validatePaymentMethod(String paymentMethod) {
        if (!Payment.METHOD_ALIPAY.equals(paymentMethod) && 
            !Payment.METHOD_WECHAT.equals(paymentMethod) && 
            !Payment.METHOD_CREDIT_CARD.equals(paymentMethod)) {
            throw new PaymentException.UnsupportedPaymentMethodException(paymentMethod);
        }
    }
    
    // 验证支付状态转换是否合法
    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (Payment.STATUS_SUCCESS.equals(currentStatus) || Payment.STATUS_FAILED.equals(currentStatus)) {
            throw new PaymentException.InvalidPaymentStatusException("支付记录状态已经是终态，不能再变更");
        }
        
        if (!Payment.STATUS_SUCCESS.equals(newStatus) && !Payment.STATUS_FAILED.equals(newStatus)) {
            throw new PaymentException.InvalidPaymentStatusException("支付状态只能更新为成功或失败");
        }
    }
} 