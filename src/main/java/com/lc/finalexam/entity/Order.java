package com.lc.finalexam.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String orderNumber;  // 订单编号

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal totalAmount;  // 订单总金额
    
    private String status;  // 订单状态: PENDING(待支付), PAID(已支付), SHIPPED(已发货), COMPLETED(已完成), CANCELLED(已取消)
    
    private String recipientName;  // 收货人姓名
    private String recipientPhone;  // 收货人电话
    private String shippingAddress;  // 收货地址
    
    private String note;  // 订单备注
    
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime payTime;     // 支付时间
    private LocalDateTime cancelTime;  // 取消时间
    private LocalDateTime shipTime;    // 发货时间
    
    private String trackingNumber;    // 物流跟踪号
    private String cancelReason;      // 取消原因

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    // 订单状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    
    // 辅助方法
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
    
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
