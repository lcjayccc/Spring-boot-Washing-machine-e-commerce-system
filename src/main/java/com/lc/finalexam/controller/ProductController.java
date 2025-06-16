package com.lc.finalexam.controller;

import com.lc.finalexam.entity.Product;
import com.lc.finalexam.entity.CategoryChild;
import com.lc.finalexam.service.ProductService;
import com.lc.finalexam.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    // 显示所有商品
    @GetMapping("/list")
    public String listProducts(@RequestParam(value = "childId", required = false) Integer childId, Model model) {
        if (childId != null) {
            List<Product> products = productService.getProductsByCategoryChildId(childId);
            model.addAttribute("products", products);
            CategoryChild category = categoryService.getChildCategoryById(childId);
            model.addAttribute("category", category);
        } else {
            model.addAttribute("products", productService.getAllProducts());
        }
        return "product_list";
    }

    // 新建商品页面
    @GetMapping("/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("types", categoryService.getChildrenByParentId(1));       // 洗衣机类型
        model.addAttribute("brands", categoryService.getChildrenByParentId(2));      // 品牌
        model.addAttribute("motorTypes", categoryService.getChildrenByParentId(3));  // 电机类型
        model.addAttribute("capacities", categoryService.getChildrenByParentId(4));  // 洗涤容量
        return "addProduct";
    }

    // 新建商品提交
    @PostMapping("/add")
    public String addProduct(
            @ModelAttribute Product product,
            @RequestParam("type") Integer typeId,
            @RequestParam("brand") Integer brandId,
            @RequestParam("motorType") Integer motorTypeId,
            @RequestParam("capacity") Integer capacityId) {

        List<Integer> categoryChildIds = List.of(typeId, brandId, motorTypeId, capacityId);
        List<CategoryChild> children = categoryChildIds.stream()
                .map(categoryService::getChildCategoryById)
                .toList();
        product.setCategories(children);
        
        // 确保状态字段有默认值
        if (product.getStatus() == null) {
            product.setStatus(Product.STATUS_ACTIVE);
        }
        
        // 如果库存为null但状态是上架，则设置默认库存为0
        if (product.getStock() == null) {
            product.setStock(0);
        }
        
        // 根据库存情况自动设置状态
        if (product.getStock() <= 0 && Product.STATUS_ACTIVE.equals(product.getStatus())) {
            product.setStatus(Product.STATUS_OUT_OF_STOCK);
        }
        
        productService.saveProduct(product);
        return "redirect:/product/list";
    }

    // 编辑商品页面
    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Integer id,
                                  @RequestParam(value = "childId", required = false) Integer childId,
                                  Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("types", categoryService.getChildrenByParentId(1));
        model.addAttribute("brands", categoryService.getChildrenByParentId(2));
        model.addAttribute("motorTypes", categoryService.getChildrenByParentId(3));
        model.addAttribute("capacities", categoryService.getChildrenByParentId(4));
        List<Integer> selectedIds = product.getCategories().stream().map(CategoryChild::getId).toList();
        model.addAttribute("selectedIds", selectedIds);
        if (childId != null) {
            CategoryChild category = categoryService.getChildCategoryById(childId);
            model.addAttribute("category", category);
        }
        return "editProduct";
    }

    // 编辑商品提交
    @PostMapping("/edit")
    public String editProduct(
            @ModelAttribute Product product,
            @RequestParam("type") Integer typeId,
            @RequestParam("brand") Integer brandId,
            @RequestParam("motorType") Integer motorTypeId,
            @RequestParam("capacity") Integer capacityId,
            @RequestParam(value = "childId", required = false) Integer childId) {

        List<Integer> categoryChildIds = List.of(typeId, brandId, motorTypeId, capacityId);
        List<CategoryChild> children = categoryChildIds.stream()
                .map(categoryService::getChildCategoryById)
                .toList();
        product.setCategories(children);
        
        // 确保状态字段有值
        if (product.getStatus() == null) {
            product.setStatus(Product.STATUS_ACTIVE);
        }
        
        // 确保库存字段有值
        if (product.getStock() == null) {
            product.setStock(0);
        }
        
        // 根据库存情况自动更新状态
        if (product.getStock() <= 0 && Product.STATUS_ACTIVE.equals(product.getStatus())) {
            product.setStatus(Product.STATUS_OUT_OF_STOCK);
        } else if (product.getStock() > 0 && Product.STATUS_OUT_OF_STOCK.equals(product.getStatus())) {
            product.setStatus(Product.STATUS_ACTIVE);
        }
        
        productService.saveProduct(product);
        // 修改后重定向回当前分类下的商品列表
        if (childId != null) {
            return "redirect:/product/list?childId=" + childId;
        } else {
            return "redirect:/product/list";
        }
    }

    // 删除商品
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id,
                                @RequestParam(value = "childId", required = false) Integer childId) {
        productService.deleteProduct(id);
        if (childId != null) {
            return "redirect:/product/list?childId=" + childId;
        } else {
            return "redirect:/product/list";
        }
    }

    // 商品搜索功能
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "childId", required = false) Integer childId,
            Model model) {
        
        List<Product> products = productService.searchProducts(keyword);
        
        // 添加搜索关键词到模型中，以便在页面上回显
        model.addAttribute("keyword", keyword);
        model.addAttribute("products", products);
        
        // 如果指定了分类，添加分类信息
        if (childId != null) {
            CategoryChild category = categoryService.getChildCategoryById(childId);
            model.addAttribute("category", category);
        }
        
        return "product_list";
    }
}