package com.lc.finalexam.controller;

import com.lc.finalexam.entity.Order;
import com.lc.finalexam.entity.Payment;
import com.lc.finalexam.entity.User;
import com.lc.finalexam.exception.OrderException;
import com.lc.finalexam.exception.PaymentException;
import com.lc.finalexam.service.OrderService;
import com.lc.finalexam.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    // 创建支付记录
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPayment(
            @RequestParam("orderId") Integer orderId,
            @RequestParam("paymentMethod") String paymentMethod,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.ok(response);
        }
        
        try {
            // 检查订单是否属于当前用户
            if (!orderService.isOrderBelongsToUser(orderId, user.getId())) {
                response.put("success", false);
                response.put("message", "订单不存在");
                return ResponseEntity.ok(response);
            }
            
            // 创建支付记录
            Payment payment = paymentService.createPayment(orderId, paymentMethod);
            
            response.put("success", true);
            response.put("paymentId", payment.getId());
            response.put("paymentNumber", payment.getPaymentNumber());
            return ResponseEntity.ok(response);
        } catch (OrderException | PaymentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建支付记录失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    // 支付处理
    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processPayment(
            @RequestParam("paymentId") Integer paymentId,
            @RequestParam("amount") BigDecimal amount,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.ok(response);
        }
        
        try {
            // 获取支付记录
            Payment payment = paymentService.getPaymentById(paymentId);
            
            // 检查订单是否属于当前用户
            if (!orderService.isOrderBelongsToUser(payment.getOrder().getId(), user.getId())) {
                response.put("success", false);
                response.put("message", "支付记录不存在");
                return ResponseEntity.ok(response);
            }
            
            // 处理支付
            Map<String, Object> result = paymentService.processPayment(paymentId, amount);
            return ResponseEntity.ok(result);
        } catch (OrderException | PaymentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "处理支付时发生错误: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    // 支付结果页面
    @GetMapping("/result/{orderId}")
    public String paymentResult(@PathVariable Integer orderId, 
                               HttpSession session, 
                               Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        
        try {
            // 检查订单是否属于当前用户
            if (!orderService.isOrderBelongsToUser(orderId, user.getId())) {
                return "redirect:/order/list";
            }
            
            Order order = orderService.getOrderById(orderId);
            
            try {
                // 尝试获取支付记录
                Payment payment = paymentService.getPaymentByOrderId(orderId);
                model.addAttribute("payment", payment);
                model.addAttribute("paymentSuccess", Payment.STATUS_SUCCESS.equals(payment.getStatus()));
            } catch (PaymentException e) {
                // 没有支付记录也不影响显示
            }
            
            model.addAttribute("order", order);
            model.addAttribute("user", user);
            
            return "payment_result";
        } catch (OrderException.OrderNotFoundException e) {
            return "redirect:/order/list";
        }
    }
    
    // 模拟支付回调（实际项目中会被第三方支付平台调用）
    @PostMapping("/callback")
    @ResponseBody
    public ResponseEntity<String> paymentCallback(
            @RequestParam("paymentNumber") String paymentNumber,
            @RequestParam("transactionId") String transactionId,
            @RequestParam("status") String status) {
        
        try {
            boolean result = paymentService.handlePaymentCallback(paymentNumber, transactionId, status);
            return ResponseEntity.ok(result ? "success" : "repeat");
        } catch (Exception e) {
            return ResponseEntity.ok("fail:" + e.getMessage());
        }
    }
} 