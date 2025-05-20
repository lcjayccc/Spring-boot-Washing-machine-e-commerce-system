package com.lc.finalexam.repository;

import com.lc.finalexam.entity.CategoryChild;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryChildRepository extends JpaRepository<CategoryChild, Integer> {
    // 查找某个父分类下的所有子分类
    List<CategoryChild> findByParent_Id(Integer parentId);

    // 按名称模糊查找
    List<CategoryChild> findByNameContaining(String name);
}