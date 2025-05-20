package com.lc.finalexam.service.impl;

import com.lc.finalexam.entity.CategoryParent;
import com.lc.finalexam.entity.CategoryChild;
import com.lc.finalexam.repository.CategoryParentRepository;
import com.lc.finalexam.repository.CategoryChildRepository;
import com.lc.finalexam.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryParentRepository parentRepo;
    @Autowired
    private CategoryChildRepository childRepo;

    @Override
    public List<CategoryParent> getAllParentCategories() {
        return parentRepo.findAll();
    }

    @Override
    public List<CategoryChild> getChildrenByParentId(Integer parentId) {
        return childRepo.findByParent_Id(parentId);
    }

    @Override
    public CategoryParent saveParentCategory(CategoryParent parent) {
        return parentRepo.save(parent);
    }

    @Override
    public CategoryChild saveChildCategory(CategoryChild child) {
        return childRepo.save(child);
    }

    @Override
    public void deleteChildCategory(Integer childId) {
        childRepo.deleteById(childId);
    }

    @Override
    public CategoryChild getChildCategoryById(Integer id) {
        return childRepo.findById(id).orElse(null);
    }

    @Override
    public CategoryParent getParentCategoryById(Integer id) {
        return parentRepo.findById(id).orElse(null);
    }
}