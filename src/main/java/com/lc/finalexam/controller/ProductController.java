package com.lc.finalexam.controller;

import com.lc.finalexam.dto.ProductQuery;
import com.lc.finalexam.entity.Product;
import com.lc.finalexam.entity.CategoryChild;
import com.lc.finalexam.service.ProductService;
import com.lc.finalexam.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // 管理端商品条件分页列表
    @GetMapping("/list")
    public String listProducts(
            @Valid @ModelAttribute("query") ProductQuery query,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/unified-login?role=admin";
        }
        if (query.getChildId() != null) {
            model.addAttribute("category", categoryService.getChildCategoryById(query.getChildId()));
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("productPage", Page.empty());
            return "product_list";
        }
        model.addAttribute("productPage", productService.queryVisibleProducts(query));
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

    @PostMapping("/{id}/off-shelf")
    public String offShelf(
            @PathVariable Integer id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/unified-login?role=admin";
        }
        try {
            productService.offShelf(id);
            redirectAttributes.addFlashAttribute("successMessage", "商品已下架");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/product/list";
    }

    // 兼容旧搜索入口，统一重定向到新的条件查询
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "childId", required = false) Integer childId,
            RedirectAttributes redirectAttributes) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            redirectAttributes.addAttribute("name", keyword.trim());
        }
        if (childId != null) {
            redirectAttributes.addAttribute("childId", childId);
        }
        return "redirect:/product/list";
    }
}
