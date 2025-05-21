package com.lc.finalexam.service.impl;

import com.lc.finalexam.entity.Product;
import com.lc.finalexam.repository.ProductRepository;
import com.lc.finalexam.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepo;

    @Override
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @Override
    public Product getProductById(Integer id) {
        return productRepo.findById(id).orElse(null);
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepo.save(product);
    }

    @Override
    public void deleteProduct(Integer id) {
        productRepo.deleteById(id);
    }

    @Override
    public List<Product> getProductsByCategoryChildId(Integer categoryChildId) {
        return productRepo.findByCategories_Id(categoryChildId);
    }
    
    @Override
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        
        // 同时搜索名称和型号，去重后返回
        Set<Product> products = new HashSet<>();
        products.addAll(productRepo.findByNameContaining(keyword));
        products.addAll(productRepo.findByModelContaining(keyword));
        
        return new ArrayList<>(products);
    }
}