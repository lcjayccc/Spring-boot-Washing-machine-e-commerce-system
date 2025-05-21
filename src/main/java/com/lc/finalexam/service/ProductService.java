package com.lc.finalexam.service;

import com.lc.finalexam.entity.Product;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Integer id);
    Product saveProduct(Product product);
    void deleteProduct(Integer id);
    List<Product> getProductsByCategoryChildId(Integer categoryChildId);
    List<Product> searchProducts(String keyword);
}