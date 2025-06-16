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
        
        String trimmedKeyword = keyword.trim();
        
        // 同时搜索名称、型号和描述，去重后返回
        Set<Product> products = new HashSet<>();
        products.addAll(productRepo.findByNameContaining(trimmedKeyword));
        products.addAll(productRepo.findByModelContaining(trimmedKeyword));
        
        // 搜索产品描述
        if (productRepo.findByDescriptionContaining(trimmedKeyword) != null) {
            products.addAll(productRepo.findByDescriptionContaining(trimmedKeyword));
        }
        
        // 将搜索结果按ID排序
        List<Product> sortedProducts = new ArrayList<>(products);
        sortedProducts.sort((p1, p2) -> p1.getId().compareTo(p2.getId()));
        
        return sortedProducts;
    }
}