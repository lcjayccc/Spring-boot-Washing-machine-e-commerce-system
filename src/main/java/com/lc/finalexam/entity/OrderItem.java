package com.lc.finalexam.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String productName;    // 下单时的商品名称
    private String productModel;   // 下单时的商品型号
    private String productImage;   // 下单时的商品图片
    private BigDecimal price;      // 下单时的商品价格
    private Integer quantity;      // 购买数量
    private BigDecimal subtotal;   // 小计金额

    // 辅助方法：计算小计
    public void calculateSubtotal() {
        if (price != null && quantity != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
