package com.lc.finalexam.controller;

import com.lc.finalexam.entity.Order;
import com.lc.finalexam.entity.User;
import com.lc.finalexam.exception.OrderException;
import com.lc.finalexam.service.OrderService;
import com.lc.finalexam.service.PaymentService;
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
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    // 创建订单
    @PostMapping("/create")
    public String createOrder(@RequestParam("recipientName") String recipientName,
                             @RequestParam("recipientPhone") String recipientPhone,
                             @RequestParam("shippingAddress") String shippingAddress,
                             @RequestParam(value = "note", required = false) String note,
                             HttpSession session, 
                             Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        
        try {
            // 从session中获取结算时选中的购物车项ID列表
            @SuppressWarnings("unchecked")
            List<Integer> cartItemIds = (List<Integer>) session.getAttribute("checkoutItemIds");
            if (cartItemIds == null || cartItemIds.isEmpty()) {
                model.addAttribute("errorMessage", "请先选择商品");
                return "redirect:/cart";
            }
            
            // 创建订单
            Order order = orderService.createOrder(
                user.getId(), cartItemIds, recipientName, recipientPhone, shippingAddress, note);
            
            // 清除session中的结算项
            session.removeAttribute("checkoutItemIds");
            
            // 跳转到支付页面
            return "redirect:/order/pay/" + order.getId();
        } catch (OrderException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "checkout";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "创建订单失败: " + e.getMessage());
            return "checkout";
        }
    }
    
    // 订单支付页面
    @GetMapping("/pay/{orderId}")
    public String payOrder(@PathVariable Integer orderId, 
                          HttpSession session, 
                          Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        
        try {
            Order order = orderService.getOrderById(orderId);
            
            // 检查订单是否属于当前用户
            if (!orderService.isOrderBelongsToUser(orderId, user.getId())) {
                return "redirect:/order/list";
            }
            
            // 检查订单状态
            if (!Order.STATUS_PENDING.equals(order.getStatus())) {
                return "redirect:/order/detail/" + orderId;
            }
            
            model.addAttribute("order", order);
            model.addAttribute("user", user);
            
            return "order_pay";
        } catch (OrderException.OrderNotFoundException e) {
            return "redirect:/order/list";
        }
    }
    
    // 用户订单列表
    @GetMapping("/list")
    public String listOrders(@RequestParam(value = "status", required = false) String status,
                            HttpSession session, 
                            Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderService.getOrdersByUserAndStatus(user.getId(), status);
        } else {
            orders = orderService.getOrdersByUser(user.getId());
        }
        
        // 获取订单统计数据
        Map<String, Long> statistics = orderService.getOrderStatistics(user.getId());
        
        model.addAttribute("orders", orders);
        model.addAttribute("statistics", statistics);
        model.addAttribute("currentStatus", status);
        model.addAttribute("user", user);
        
        return "order_list";
    }
    
    // 订单详情
    @GetMapping("/detail/{orderId}")
    public String orderDetail(@PathVariable Integer orderId, 
                             HttpSession session, 
                             Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        
        try {
            Order order = orderService.getOrderById(orderId);
            
            // 检查订单是否属于当前用户
            if (!orderService.isOrderBelongsToUser(orderId, user.getId())) {
                return "redirect:/order/list";
            }
            
            model.addAttribute("order", order);
            model.addAttribute("user", user);
            
            return "order_detail";
        } catch (OrderException.OrderNotFoundException e) {
            return "redirect:/order/list";
        }
    }
    
    // 取消订单
    @PostMapping("/cancel/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Integer orderId, 
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
            
            // 取消订单
            Order order = orderService.cancelOrder(orderId);
            
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