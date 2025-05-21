package com.lc.finalexam.controller;

import com.lc.finalexam.entity.CartItem;
import com.lc.finalexam.entity.User;
import com.lc.finalexam.service.CartService;
import com.lc.finalexam.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    // 查看购物车
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";

        List<CartItem> cartItems = cartService.getCartItems(user.getId());
        double total = cartService.calculateCartTotal(user.getId());

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("user", user);
        return "cart";
    }

    // 添加商品到购物车
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam("productId") Integer productId,
            @RequestParam("quantity") Integer quantity,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.ok(response);
        }
        
        try {
            cartService.addProductToCart(user.getId(), productId, quantity);
            response.put("success", true);
            response.put("message", "商品已成功添加到购物车");
            response.put("cartCount", cartService.getCartItems(user.getId()).size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    // 从购物车中移除商品
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @RequestParam("productId") Integer productId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.ok(response);
        }
        
        try {
            cartService.removeProductFromCart(user.getId(), productId);
            
            // 获取更新后的购物车信息
            List<CartItem> cartItems = cartService.getCartItems(user.getId());
            double total = cartService.calculateCartTotal(user.getId());
            
            response.put("success", true);
            response.put("message", "商品已从购物车移除");
            response.put("cartItems", cartItems);
            response.put("total", total);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    // 批量移除商品
    @DeleteMapping("/removeMultiple")
    public ResponseEntity<Map<String, Object>> removeMultipleItems(
            @RequestParam("productIds") List<Integer> productIds,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.ok(response);
        }
        
        try {
            cartService.removeProductsFromCart(user.getId(), productIds);
            
            // 获取更新后的购物车信息，只返回必要信息
            double total = cartService.calculateCartTotal(user.getId());
            
            response.put("success", true);
            response.put("message", "所选商品已从购物车移除");
            response.put("total", total);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    // 更新购物车商品数量
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @RequestParam("productId") Integer productId,
            @RequestParam("quantity") Integer quantity,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.ok(response);
        }
        
        try {
            // 在更新之前，获取当前商品的数量
            List<CartItem> currentCartItems = cartService.getCartItems(user.getId());
            Integer currentQuantity = null;
            for (CartItem item : currentCartItems) {
                if (item.getProduct().getId().equals(productId)) {
                    currentQuantity = item.getQuantity();
                    break;
                }
            }
            
            cartService.updateProductQuantity(user.getId(), productId, quantity);
            
            // 获取更新后的购物车信息
            List<CartItem> cartItems = cartService.getCartItems(user.getId());
            double total = cartService.calculateCartTotal(user.getId());
            
            // 构建含有更新后单个商品小计的响应
            double itemSubtotal = 0;
            for (CartItem item : cartItems) {
                if (item.getProduct().getId().equals(productId) && item.getProduct().getPrice() != null) {
                    itemSubtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())).doubleValue();
                    break;
                }
            }
            
            response.put("success", true);
            response.put("message", "商品数量已更新");
            response.put("subtotal", itemSubtotal);
            response.put("total", total);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            // 如果更新失败，返回当前数量
            for (CartItem item : cartService.getCartItems(user.getId())) {
                if (item.getProduct().getId().equals(productId)) {
                    response.put("currentQuantity", item.getQuantity());
                    break;
                }
            }
            return ResponseEntity.ok(response);
        }
    }

    // 清空购物车
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.ok(response);
        }
        
        try {
            cartService.clearCart(user.getId());
            response.put("success", true);
            response.put("message", "购物车已清空");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    // 计算选中商品的总价
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateSelected(
            @RequestParam("itemIds") List<Integer> itemIds,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.ok(response);
        }
        
        try {
            Map<String, Object> result = cartService.calculateSelectedItems(user.getId(), itemIds);
            response.put("success", true);
            response.put("selectedTotal", result.get("total"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    // 购物车结算页面
    @PostMapping("/checkout")
    public String checkout(@RequestParam("itemIds") List<Integer> itemIds,
                          HttpSession session, 
                          Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        
        try {
            Map<String, Object> result = cartService.calculateSelectedItems(user.getId(), itemIds);
            List<CartItem> selectedItems = (List<CartItem>) result.get("selectedItems");
            double total = (Double) result.get("total");
            
            // 将选中的商品ID存入session，供后续订单创建使用
            session.setAttribute("checkoutItemIds", itemIds);
            
            model.addAttribute("selectedItems", selectedItems);
            model.addAttribute("total", total);
            model.addAttribute("user", user);
            
            return "checkout";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }
} 