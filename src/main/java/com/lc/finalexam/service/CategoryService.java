package com.lc.finalexam.service;

import com.lc.finalexam.entity.CategoryChild;
import com.lc.finalexam.entity.CategoryParent;
import java.util.List;

public interface CategoryService {
    List<CategoryParent> getAllParentCategories();
    List<CategoryChild> getChildrenByParentId(Integer parentId);
    CategoryParent saveParentCategory(CategoryParent parent);
    CategoryChild saveChildCategory(CategoryChild child);
    void deleteChildCategory(Integer childId);
    CategoryChild getChildCategoryById(Integer id);
    CategoryParent getParentCategoryById(Integer id);
}