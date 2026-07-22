package com.lc.finalexam.repository;

import com.lc.finalexam.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    List<Product> findByCategories_IdAndStatusIn(Integer categoryChildId, Collection<String> statuses);
}
