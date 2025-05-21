package com.lc.finalexam.repository;

import com.lc.finalexam.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // 根据订单ID查找支付记录
    Optional<Payment> findByOrder_Id(Integer orderId);
    
    // 根据支付流水号查找支付记录
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    
    // 根据第三方交易ID查找支付记录
    Optional<Payment> findByTransactionId(String transactionId);
} 