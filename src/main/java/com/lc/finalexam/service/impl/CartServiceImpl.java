package com.lc.finalexam.service.impl;

import com.lc.finalexam.entity.Cart;
import com.lc.finalexam.entity.CartItem;
import com.lc.finalexam.entity.Product;
import com.lc.finalexam.entity.User;
import com.lc.finalexam.repository.CartItemRepository;
import com.lc.finalexam.repository.CartRepository;
import com.lc.finalexam.repository.ProductRepository;
import com.lc.finalexam.repository.UserRepository;
import com.lc.finalexam.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public Cart getOrCreateCart(Integer userId) {
        Cart cart = cartRepository.findByUser_Id(userId);
        if (cart == null) {
            // 如果购物车不存在，则创建新购物车
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            cart = new Cart();
            cart.setUser(user);
            cart.setItems(new ArrayList<>());
            cartRepository.save(cart);
        }
        return cart;
    }

    @Override
    @Transactional
    public CartItem addProductToCart(Integer userId, Integer productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("商品数量必须大于0");
        }
        
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 检查产品库存
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }
        
        // 检查购物车是否已有该商品
        CartItem cartItem = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId);
        
        if (cartItem == null) {
            // 如果购物车中没有该商品，创建新项
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
        } else {
            // 如果购物车中已有该商品，增加数量
            int newQuantity = cartItem.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("商品库存不足");
            }
            cartItem.setQuantity(newQuantity);
        }
        
        cartItemRepository.save(cartItem);
        return cartItem;
    }

    @Override
    @Transactional
    public void removeProductFromCart(Integer userId, Integer productId) {
        Cart cart = cartRepository.findByUser_Id(userId);
        if (cart != null) {
            CartItem cartItem = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId);
            if (cartItem != null) {
                cartItemRepository.delete(cartItem);
            }
        }
    }

    @Override
    @Transactional
    public void removeProductsFromCart(Integer userId, List<Integer> productIds) {
        Cart cart = cartRepository.findByUser_Id(userId);
        if (cart != null && productIds != null && !productIds.isEmpty()) {
            for (Integer productId : productIds) {
                CartItem cartItem = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId);
                if (cartItem != null) {
                    cartItemRepository.delete(cartItem);
                }
            }
        }
    }

    @Override
    @Transactional
    public CartItem updateProductQuantity(Integer userId, Integer productId, Integer quantity) {
        if (quantity <= 0) {
            // 如果数量小于等于0，直接移除该商品
            removeProductFromCart(userId, productId);
            return null;
        }
        
        Cart cart = cartRepository.findByUser_Id(userId);
        if (cart == null) {
            throw new RuntimeException("购物车不存在");
        }
        
        CartItem cartItem = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId);
        if (cartItem == null) {
            throw new RuntimeException("购物车中无此商品");
        }
        
        // 检查库存
        Product product = cartItem.getProduct();
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }
        
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Integer userId) {
        Cart cart = cartRepository.findByUser_Id(userId);
        if (cart == null) {
            return new ArrayList<>();
        }
        
        Iterable<CartItem> items = cartItemRepository.findByCart_Id(cart.getId());
        List<CartItem> itemList = new ArrayList<>();
        items.forEach(itemList::add);
        return itemList;
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateCartTotal(Integer userId) {
        List<CartItem> cartItems = getCartItems(userId);
        return cartItems.stream()
                .mapToDouble(item -> {
                    BigDecimal price = item.getProduct().getPrice();
                    return price != null ? price.multiply(BigDecimal.valueOf(item.getQuantity())).doubleValue() : 0;
                })
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> calculateSelectedItems(Integer userId, List<Integer> itemIds) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取购物车中所有商品
        List<CartItem> allItems = getCartItems(userId);
        
        // 筛选出选中的商品
        List<CartItem> selectedItems = allItems.stream()
                .filter(item -> itemIds.contains(item.getId()))
                .collect(Collectors.toList());
        
        // 计算选中商品的总价
        double total = selectedItems.stream()
                .mapToDouble(item -> {
                    BigDecimal price = item.getProduct().getPrice();
                    return price != null ? price.multiply(BigDecimal.valueOf(item.getQuantity())).doubleValue() : 0;
                })
                .sum();
        
        result.put("selectedItems", selectedItems);
        result.put("total", total);
        
        return result;
    }

    @Override
    @Transactional
    public void clearCart(Integer userId) {
        Cart cart = cartRepository.findByUser_Id(userId);
        if (cart != null) {
            List<CartItem> cartItems = (List<CartItem>) cartItemRepository.findByCart_Id(cart.getId());
            cartItemRepository.deleteAll(cartItems);
        }
    }
} 