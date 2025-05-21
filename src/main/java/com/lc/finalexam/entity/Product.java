package com.lc.finalexam.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "product")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String model;
    private BigDecimal price;
    private String motorType;
    private String capacity;
    private String imageUrl;
    private String description;

    private Integer stock; // 库存数量
    private String status; // 商品状态，如"上架"/"下架"
    
    // 产品状态常量
    public static final String STATUS_ACTIVE = "ACTIVE";    // 上架
    public static final String STATUS_INACTIVE = "INACTIVE"; // 下架
    public static final String STATUS_OUT_OF_STOCK = "OUT_OF_STOCK"; // 缺货
    
    // 库存操作方法
    public void decreaseStock(int quantity) {
        if (this.stock != null && this.stock >= quantity) {
            this.stock -= quantity;
            // 检查库存是否为0，如果是，则更新状态
            if (this.stock == 0) {
                this.status = STATUS_OUT_OF_STOCK;
            }
        } else {
            throw new IllegalStateException("库存不足");
        }
    }
    
    public void increaseStock(int quantity) {
        if (this.stock == null) {
            this.stock = quantity;
        } else {
            this.stock += quantity;
        }
        // 如果之前是缺货状态，现在有库存了，则更新状态为上架
        if (STATUS_OUT_OF_STOCK.equals(this.status) && this.stock > 0) {
            this.status = STATUS_ACTIVE;
        }
    }
    
    // 锁定库存（预留），订单创建时调用
    public void lockStock(int quantity) {
        if (this.stock != null && this.stock >= quantity) {
            this.stock -= quantity;
        } else {
            throw new IllegalStateException("库存不足，无法锁定");
        }
    }
    
    // 释放库存，订单取消时调用
    public void unlockStock(int quantity) {
        increaseStock(quantity);
    }

    @ManyToMany
    @JoinTable(
            name = "product_category_mapping",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_child_id")
    )
    private List<CategoryChild> categories;
}
