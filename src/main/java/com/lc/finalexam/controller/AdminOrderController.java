package com.lc.finalexam.controller;

import com.lc.finalexam.entity.Order;
import com.lc.finalexam.entity.User;
import com.lc.finalexam.exception.OrderException;
import com.lc.finalexam.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    // 管理员订单列表页面
    @GetMapping("/list")
    public String listOrders(@RequestParam(value = "status", required = false) String status,
                            HttpSession session, 
                            Model model) {
        
        User admin = (User) session.getAttribute("admin");
        if (admin == null) return "redirect:/login";
        
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderService.getAllOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }
        
        // 获取订单统计数据
        Map<String, Long> statistics = orderService.getAdminOrderStatistics();
        
        model.addAttribute("orders", orders);
        model.addAttribute("statistics", statistics);
        model.addAttribute("currentStatus", status);
        model.addAttribute("admin", admin);
        
        return "admin_order_list";
    }
    
    // 管理员订单详情页面
    @GetMapping("/detail/{orderId}")
    public String orderDetail(@PathVariable Integer orderId, 
                             HttpSession session, 
                             Model model) {
        
        User admin = (User) session.getAttribute("admin");
        if (admin == null) return "redirect:/login";
        
        try {
            Order order = orderService.getOrderById(orderId);
            model.addAttribute("order", order);
            model.addAttribute("admin", admin);
            
            return "admin_order_detail";
        } catch (OrderException.OrderNotFoundException e) {
            return "redirect:/admin/order/list";
        }
    }
    
    // 管理员发货处理
    @PostMapping("/ship/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> shipOrder(
            @PathVariable Integer orderId,
            @RequestParam(value = "trackingNumber", required = false) String trackingNumber,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User admin = (User) session.getAttribute("admin");
        
        if (admin == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.ok(response);
        }
        
        try {
            // 更新订单状态为已发货
            Order order = orderService.shipOrder(orderId, trackingNumber);
            
            response.put("success", true);
            response.put("message", "订单已发货");
            response.put("orderId", order.getId());
            return ResponseEntity.ok(response);
        } catch (OrderException.InvalidOrderStatusException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "处理订单失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    // 管理员取消订单
    @PostMapping("/cancel/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Integer orderId,
            @RequestParam("reason") String reason,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User admin = (User) session.getAttribute("admin");
        
        if (admin == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.ok(response);
        }
        
        try {
            // 管理员取消订单
            Order order = orderService.adminCancelOrder(orderId, reason);
            
            response.put("success", true);
            response.put("message", "订单已取消");
            response.put("orderId", order.getId());
            return ResponseEntity.ok(response);
        } catch (OrderException.InvalidOrderStatusException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "取消订单失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
} 