package com.lc.finalexam.service.impl;

import com.lc.finalexam.dto.ProductQuery;
import com.lc.finalexam.entity.Product;
import com.lc.finalexam.repository.ProductRepository;
import com.lc.finalexam.repository.ProductSpecifications;
import com.lc.finalexam.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private static final List<String> VISIBLE_STATUSES = List.of(
            Product.STATUS_ACTIVE, Product.STATUS_OUT_OF_STOCK);

    private final ProductRepository productRepo;

    public ProductServiceImpl(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> queryVisibleProducts(ProductQuery query) {
        Pageable pageable = PageRequest.of(
                query.getPageNum() - 1,
                query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id"));
        return productRepo.findAll(ProductSpecifications.visibleWith(query), pageable);
    }

    @Override
    @Transactional
    public void offShelf(Integer id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        product.offShelf();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepo.findAll(
                ProductSpecifications.visibleWith(new ProductQuery()),
                Sort.by(Sort.Direction.DESC, "id"));
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
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategoryChildId(Integer categoryChildId) {
        return productRepo.findByCategories_IdAndStatusIn(categoryChildId, VISIBLE_STATUSES);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        ProductQuery query = new ProductQuery();
        query.setName(keyword);
        return productRepo.findAll(
                ProductSpecifications.visibleWith(query),
                Sort.by(Sort.Direction.DESC, "id"));
    }
}
