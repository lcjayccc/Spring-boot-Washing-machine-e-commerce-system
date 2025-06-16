package com.lc.finalexam.repository;

import com.lc.finalexam.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // 按名称模糊查找
    List<Product> findByNameContaining(String name);

    // 按型号模糊查找
    List<Product> findByModelContaining(String model);

    // 按描述模糊查找
    List<Product> findByDescriptionContaining(String description);

    // 查找属于某个子分类的商品
    List<Product> findByCategories_Id(Integer categoryChildId);
}
