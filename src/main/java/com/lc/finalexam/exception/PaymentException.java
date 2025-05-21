package com.lc.finalexam.exception;

public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
    
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // 支付不存在异常
    public static class PaymentNotFoundException extends PaymentException {
        public PaymentNotFoundException(Integer paymentId) {
            super("支付记录不存在: " + paymentId);
        }
    }
    
    // 支付状态异常
    public static class InvalidPaymentStatusException extends PaymentException {
        public InvalidPaymentStatusException(String message) {
            super(message);
        }
    }
    
    // 支付创建异常
    public static class PaymentCreationException extends PaymentException {
        public PaymentCreationException(String message) {
            super("创建支付记录失败: " + message);
        }
        
        public PaymentCreationException(String message, Throwable cause) {
            super("创建支付记录失败: " + message, cause);
        }
    }
    
    // 支付处理异常
    public static class PaymentProcessingException extends PaymentException {
        public PaymentProcessingException(String message) {
            super("支付处理失败: " + message);
        }
        
        public PaymentProcessingException(String message, Throwable cause) {
            super("支付处理失败: " + message, cause);
        }
    }
    
    // 支付方式不支持异常
    public static class UnsupportedPaymentMethodException extends PaymentException {
        public UnsupportedPaymentMethodException(String paymentMethod) {
            super("不支持的支付方式: " + paymentMethod);
        }
    }
    
    // 支付金额不匹配异常
    public static class PaymentAmountMismatchException extends PaymentException {
        public PaymentAmountMismatchException(Double expectedAmount, Double actualAmount) {
            super(String.format("支付金额不匹配，应付: %.2f, 实付: %.2f", expectedAmount, actualAmount));
        }
    }
} 