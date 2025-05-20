package com.lc.finalexam.controller;

import com.lc.finalexam.entity.CategoryParent;
import com.lc.finalexam.entity.CategoryChild;
import com.lc.finalexam.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    // 显示所有分类
    @GetMapping("/list")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllParentCategories());
        return "category_list";
    }

    // 新建子分类页面
    @GetMapping("/addChild")
    public String addChildForm(Model model) {
        model.addAttribute("parentCategories", categoryService.getAllParentCategories());
        model.addAttribute("child", new CategoryChild());
        return "addChildCategory";
    }

    // 新建子分类提交
    @PostMapping("/addChild")
    public String addChild(@ModelAttribute CategoryChild child, @RequestParam Integer parentId) {
        CategoryParent parent = categoryService.getParentCategoryById(parentId);
        child.setParent(parent);
        categoryService.saveChildCategory(child);
        return "redirect:/category/list";
    }

    // 编辑子分类页面
    @GetMapping("/editChild/{id}")
    public String editChildForm(@PathVariable Integer id, Model model) {
        model.addAttribute("child", categoryService.getChildCategoryById(id));
        model.addAttribute("parentCategories", categoryService.getAllParentCategories());
        return "editCategory";
    }

    // 编辑子分类提交
    @PostMapping("/editChild")
    public String editChild(@ModelAttribute CategoryChild child, @RequestParam Integer parentId) {
        CategoryParent parent = categoryService.getParentCategoryById(parentId);
        child.setParent(parent);
        categoryService.saveChildCategory(child);
        return "redirect:/category/list";
    }

    // 删除子分类
    @GetMapping("/deleteChild/{id}")
    public String deleteChild(@PathVariable Integer id) {
        categoryService.deleteChildCategory(id);
        return "redirect:/category/list";
    }
}