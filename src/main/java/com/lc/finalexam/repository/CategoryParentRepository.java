package com.lc.finalexam.repository;


import com.lc.finalexam.entity.CategoryParent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryParentRepository extends JpaRepository<CategoryParent, Integer> {
    // 按名称模糊查找
    List<CategoryParent> findByNameContaining(String name);
}