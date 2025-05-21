package com.lc.finalexam.service.impl;

import com.lc.finalexam.entity.*;
import com.lc.finalexam.exception.OrderException;
import com.lc.finalexam.repository.CartItemRepository;
import com.lc.finalexam.repository.OrderItemRepository;
import com.lc.finalexam.repository.OrderRepository;
import com.lc.finalexam.repository.UserRepository;
import com.lc.finalexam.service.OrderService;
import com.lc.finalexam.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductService productService;

    @Override
    @Transactional
    public Order createOrder(Integer userId, List<Integer> cartItemIds, String recipientName, 
                           String recipientPhone, String shippingAddress, String note) {
        // 检验参数
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new OrderException.EmptyOrderItemsException();
        }
        
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderException.OrderCreationException("用户不存在"));
        
        // 获取购物车项
        List<CartItem> cartItems = cartItemRepository.findByIdIn(cartItemIds);
        if (cartItems.isEmpty()) {
            throw new OrderException.EmptyOrderItemsException();
        }
        
        // 创建订单
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(Order.STATUS_PENDING);
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setShippingAddress(shippingAddress);
        order.setNote(note);
        order.setCreateTime(LocalDateTime.now());
        
        // 计算总金额并创建订单项
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 锁定商品库存
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            Integer quantity = cartItem.getQuantity();
            
            // 检查库存是否充足
            if (product.getStock() == null || product.getStock() < quantity) {
                throw new OrderException.InsufficientStockException(product.getId());
            }
            
            try {
                // 锁定库存
                product.lockStock(quantity);
                productService.saveProduct(product);
                
                // 创建订单项
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setProductName(product.getName());
                orderItem.setProductModel(product.getModel());
                orderItem.setProductImage(product.getImageUrl());
                orderItem.setPrice(product.getPrice());
                orderItem.setQuantity(quantity);
                orderItem.calculateSubtotal();
                
                order.addOrderItem(orderItem);
                
                // 累加总金额
                if (orderItem.getSubtotal() != null) {
                    totalAmount = totalAmount.add(orderItem.getSubtotal());
                }
            } catch (Exception e) {
                // 如果发生任何异常，回滚事务
                throw new OrderException.OrderCreationException("创建订单项失败: " + e.getMessage(), e);
            }
        }
        
        order.setTotalAmount(totalAmount);
        
        // 保存订单
        try {
            orderRepository.save(order);
            
            // 从购物车中删除已购买的商品
            cartItemRepository.deleteAll(cartItems);
            
            return order;
        } catch (Exception e) {
            throw new OrderException.OrderCreationException("保存订单失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(orderId));
    }

    @Override
    public Order getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if (order == null) {
            throw new OrderException.OrderNotFoundException(0);
        }
        return order;
    }

    @Override
    public List<Order> getOrdersByUser(Integer userId) {
        return orderRepository.findByUser_IdOrderByCreateTimeDesc(userId);
    }

    @Override
    public List<Order> getOrdersByUserAndStatus(Integer userId, String status) {
        return orderRepository.findByUser_IdAndStatusOrderByCreateTimeDesc(userId, status);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Integer orderId, String status) {
        Order order = getOrderById(orderId);
        
        // 检查状态转换是否合法
        validateStatusTransition(order.getStatus(), status);
        
        order.setStatus(status);
        
        // 如果状态是已支付，设置支付时间
        if (Order.STATUS_PAID.equals(status)) {
            order.setPayTime(LocalDateTime.now());
            
            // 支付成功后，库存已经锁定，不需要额外操作
        } 
        // 如果状态是已取消，设置取消时间，并释放库存
        else if (Order.STATUS_CANCELLED.equals(status)) {
            order.setCancelTime(LocalDateTime.now());
            releaseOrderStock(order);
        }
        
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(Integer orderId) {
        Order order = getOrderById(orderId);
        
        // 只有待支付状态的订单可以取消
        if (!Order.STATUS_PENDING.equals(order.getStatus())) {
            throw new OrderException.InvalidOrderStatusException("只有待支付状态的订单可以取消");
        }
        
        // 更新订单状态
        order.setStatus(Order.STATUS_CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        
        // 释放库存
        releaseOrderStock(order);
        
        return orderRepository.save(order);
    }
    
    // 释放订单锁定的库存
    private void releaseOrderStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.unlockStock(item.getQuantity());
            productService.saveProduct(product);
        }
    }
    
    // 验证订单状态转换是否合法
    private void validateStatusTransition(String currentStatus, String newStatus) {
        // 根据业务规则，定义合法的状态转换
        Map<String, List<String>> validTransitions = new HashMap<>();
        validTransitions.put(Order.STATUS_PENDING, Arrays.asList(Order.STATUS_PAID, Order.STATUS_CANCELLED));
        validTransitions.put(Order.STATUS_PAID, Arrays.asList(Order.STATUS_SHIPPED, Order.STATUS_CANCELLED));
        validTransitions.put(Order.STATUS_SHIPPED, Arrays.asList(Order.STATUS_COMPLETED));
        validTransitions.put(Order.STATUS_COMPLETED, Collections.emptyList());
        validTransitions.put(Order.STATUS_CANCELLED, Collections.emptyList());
        
        List<String> allowedStatuses = validTransitions.getOrDefault(currentStatus, Collections.emptyList());
        if (!allowedStatuses.contains(newStatus)) {
            throw new OrderException.InvalidOrderStatusException(
                    String.format("订单状态不能从 %s 变更为 %s", currentStatus, newStatus));
        }
    }

    @Override
    public String generateOrderNumber() {
        // 生成格式为：年月日时分秒+6位随机数的订单号
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", new Random().nextInt(1000000));
        return timestamp + random;
    }

    @Override
    public boolean isOrderBelongsToUser(Integer orderId, Integer userId) {
        Order order = getOrderById(orderId);
        return order.getUser().getId().equals(userId);
    }

    @Override
    public Map<String, Long> getOrderStatistics(Integer userId) {
        List<Order> orders = getOrdersByUser(userId);
        
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("total", (long) orders.size());
        
        // 按状态分组统计
        Map<String, Long> countByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        
        statistics.put("pending", countByStatus.getOrDefault(Order.STATUS_PENDING, 0L));
        statistics.put("paid", countByStatus.getOrDefault(Order.STATUS_PAID, 0L));
        statistics.put("shipped", countByStatus.getOrDefault(Order.STATUS_SHIPPED, 0L));
        statistics.put("completed", countByStatus.getOrDefault(Order.STATUS_COMPLETED, 0L));
        statistics.put("cancelled", countByStatus.getOrDefault(Order.STATUS_CANCELLED, 0L));
        
        return statistics;
    }
    
    // === 管理员功能实现 ===
    
    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreateTimeDesc();
    }
    
    @Override
    public List<Order> getAllOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }
    
    @Override
    public Map<String, Long> getAdminOrderStatistics() {
        List<Order> orders = getAllOrders();
        
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("total", (long) orders.size());
        
        // 按状态分组统计
        Map<String, Long> countByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        
        statistics.put("pending", countByStatus.getOrDefault(Order.STATUS_PENDING, 0L));
        statistics.put("paid", countByStatus.getOrDefault(Order.STATUS_PAID, 0L));
        statistics.put("shipped", countByStatus.getOrDefault(Order.STATUS_SHIPPED, 0L));
        statistics.put("completed", countByStatus.getOrDefault(Order.STATUS_COMPLETED, 0L));
        statistics.put("cancelled", countByStatus.getOrDefault(Order.STATUS_CANCELLED, 0L));
        
        return statistics;
    }
    
    @Override
    @Transactional
    public Order shipOrder(Integer orderId, String trackingNumber) {
        Order order = getOrderById(orderId);
        
        // 只有已支付状态的订单可以发货
        if (!Order.STATUS_PAID.equals(order.getStatus())) {
            throw new OrderException.InvalidOrderStatusException("只有已支付状态的订单可以发货");
        }
        
        // 更新订单状态
        order.setStatus(Order.STATUS_SHIPPED);
        order.setShipTime(LocalDateTime.now());
        order.setTrackingNumber(trackingNumber);
        
        return orderRepository.save(order);
    }
    
    @Override
    @Transactional
    public Order adminCancelOrder(Integer orderId, String reason) {
        Order order = getOrderById(orderId);
        
        // 管理员可以取消待支付或已支付状态的订单
        if (!Order.STATUS_PENDING.equals(order.getStatus()) && !Order.STATUS_PAID.equals(order.getStatus())) {
            throw new OrderException.InvalidOrderStatusException("只有待支付或已支付状态的订单可以取消");
        }
        
        // 更新订单状态
        order.setStatus(Order.STATUS_CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        
        // 释放库存
        releaseOrderStock(order);
        
        return orderRepository.save(order);
    }
} 