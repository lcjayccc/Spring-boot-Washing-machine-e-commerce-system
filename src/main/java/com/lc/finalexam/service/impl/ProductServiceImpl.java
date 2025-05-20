package com.lc.finalexam.service.impl;

import com.lc.finalexam.entity.Product;
import com.lc.finalexam.repository.ProductRepository;
import com.lc.finalexam.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}