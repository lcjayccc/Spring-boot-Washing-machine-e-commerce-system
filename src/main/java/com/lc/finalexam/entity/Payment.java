package com.lc.finalexam.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    private String paymentNumber;   // 支付流水号
    private BigDecimal amount;      // 支付金额
    private String paymentMethod;   // 支付方式：ALIPAY, WECHAT, CREDIT_CARD
    private String status;          // 支付状态：PENDING, SUCCESS, FAILED
    private String transactionId;   // 第三方支付平台交易ID
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime payTime;  // 支付完成时间
    
    // 支付方式常量
    public static final String METHOD_ALIPAY = "ALIPAY";
    public static final String METHOD_WECHAT = "WECHAT";
    public static final String METHOD_CREDIT_CARD = "CREDIT_CARD";
    
    // 支付状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (status == null) {
            status = STATUS_PENDING;
        }
    }
} 