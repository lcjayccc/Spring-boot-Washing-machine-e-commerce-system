package com.lc.finalexam.service;

import com.lc.finalexam.dto.ProductQuery;
import com.lc.finalexam.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    Page<Product> queryVisibleProducts(ProductQuery query);
    void offShelf(Integer id);
    List<Product> getAllProducts();
    Product getProductById(Integer id);
    Product saveProduct(Product product);
    void deleteProduct(Integer id);
    List<Product> getProductsByCategoryChildId(Integer categoryChildId);
    List<Product> searchProducts(String keyword);
}
