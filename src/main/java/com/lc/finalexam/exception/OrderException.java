package com.lc.finalexam.exception;

public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }
    
    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // 订单不存在异常
    public static class OrderNotFoundException extends OrderException {
        public OrderNotFoundException(Integer orderId) {
            super("订单不存在: " + orderId);
        }
    }
    
    // 订单状态操作异常
    public static class InvalidOrderStatusException extends OrderException {
        public InvalidOrderStatusException(String message) {
            super(message);
        }
    }
    
    // 库存不足异常
    public static class InsufficientStockException extends OrderException {
        public InsufficientStockException(Integer productId) {
            super("商品库存不足: " + productId);
        }
    }
    
    // 订单创建异常
    public static class OrderCreationException extends OrderException {
        public OrderCreationException(String message) {
            super("创建订单失败: " + message);
        }
        
        public OrderCreationException(String message, Throwable cause) {
            super("创建订单失败: " + message, cause);
        }
    }
    
    // 订单项为空异常
    public static class EmptyOrderItemsException extends OrderException {
        public EmptyOrderItemsException() {
            super("订单中没有商品项");
        }
    }
    
    // 订单取消异常
    public static class OrderCancellationException extends OrderException {
        public OrderCancellationException(String message) {
            super("取消订单失败: " + message);
        }
    }
} 