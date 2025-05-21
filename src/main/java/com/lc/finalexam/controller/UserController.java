package com.lc.finalexam.controller;

import com.lc.finalexam.dto.UserRegisterDTO;
import com.lc.finalexam.dto.UserLoginDTO;
import com.lc.finalexam.dto.UserProfileDTO;
import com.lc.finalexam.entity.Product;
import com.lc.finalexam.entity.User;
import com.lc.finalexam.entity.CategoryParent;
import com.lc.finalexam.entity.CategoryChild;
import com.lc.finalexam.service.UserService;
import com.lc.finalexam.service.ProductService;
import com.lc.finalexam.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.MessageSource;
import java.util.Locale;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MessageSource messageSource;

    // 跳转到用户登录页面
    @GetMapping("/login")
    public String loginPage() {
        return "user_login";
    }

    // 用户登录表单处理
    @PostMapping("/login")
    public String login(@ModelAttribute @Valid UserLoginDTO dto, HttpSession session, Model model) {
        User user = null;
        if (dto.getLoginKey() != null) {
            user = userService.getUserByUsername(dto.getLoginKey());
            if (user == null) user = userService.getUserById(tryParseInt(dto.getLoginKey()));
        }
        if (user != null && user.getPassword().equals(dto.getPassword())) {
            session.setAttribute("user", user);
            model.addAttribute("message", "登录成功");
            return "redirect:/user/index";
        }
        model.addAttribute("errorMessage", "用户名/邮箱/手机号或密码错误");
        model.addAttribute("loginKey", dto.getLoginKey());
        return "user_login";
    }

    // 跳转到用户注册页面
    @GetMapping("/register")
    public String registerPage() {
        return "user_register";
    }

    // 用户注册表单处理
    @PostMapping("/register")
    public String register(@ModelAttribute @Valid UserRegisterDTO dto, BindingResult bindingResult, Model model, Locale locale) {
        if (bindingResult.hasErrors()) {
            String key = bindingResult.getFieldError().getDefaultMessage();
            String errorMsg = messageSource.getMessage(key, null, key, locale);
            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("username", dto.getUsername());
            model.addAttribute("email", dto.getEmail());
            model.addAttribute("phone", dto.getPhone());
            return "user_register";
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        User saved = userService.register(user);
        if (saved == null) {
            model.addAttribute("errorMessage", "用户名/邮箱/手机号已存在");
            model.addAttribute("username", dto.getUsername());
            model.addAttribute("email", dto.getEmail());
            model.addAttribute("phone", dto.getPhone());
            return "user_register";
        }
        model.addAttribute("message", "注册成功，请登录");
        return "redirect:/user/login";
    }


    // 跳转到编辑个人信息页面
    @GetMapping("/profile")
    public String editProfilePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        model.addAttribute("user", userService.getUserById(user.getId()));
        return "editProfile";
    }

    // 处理个人信息修改
    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam(required = false) String password,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            HttpSession session,
            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        if (password != null && !password.isEmpty()) {
            user.setPassword(password);
        }
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String fileName = org.springframework.util.StringUtils.cleanPath(avatar.getOriginalFilename());
                String uploadDir = "E:/Learn/软件架构/大作业/lcwork/finalexam/images/";
                java.io.File dir = new java.io.File(uploadDir);
                if (!dir.exists()) dir.mkdirs();
                java.io.File dest = new java.io.File(uploadDir + fileName);
                avatar.transferTo(dest);
                String avatarUrl = "/images/" + fileName;
                user.setAvatarUrl(avatarUrl);
            } catch (Exception e) {
                model.addAttribute("errorMessage", "头像上传失败");
                model.addAttribute("user", user);
                return "editProfile";
            }
        }
        userService.updateProfile(user.getId(), user.getEmail(), user.getPhone(), user.getAvatarUrl());
        User updatedUser = userService.getUserById(user.getId());
        session.setAttribute("user", updatedUser);
        model.addAttribute("user", updatedUser);
        model.addAttribute("message", "修改成功");
        return "redirect:/user/index";
    }

    // 头像上传
    @PostMapping("/avatar")
    public String uploadAvatar(@RequestParam MultipartFile file, HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) return "未登录";
        if (file.isEmpty()) return "未选择文件";
        String ext = org.springframework.util.StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = System.currentTimeMillis() + (ext != null ? ("." + ext) : "");
        String uploadDir = "E:/Learn/软件架构/大作业/lcwork/finalexam/images/avatar/";
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        java.io.File dest = new java.io.File(uploadDir + fileName);
        file.transferTo(dest);
        String avatarUrl = "/images/avatar/" + fileName;
        userService.updateProfile(user.getId(), null, null, avatarUrl);
        user.setAvatarUrl(avatarUrl);
        session.setAttribute("user", user);
        return avatarUrl;
    }

    // 用户首页 - 显示所有商品和分类
    @GetMapping("/index")
    public String userIndex(Model model, HttpSession session) {
        List<Product> products = productService.getAllProducts();
        List<CategoryParent> categories = categoryService.getAllParentCategories();
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("user", user);
        return "user_product_list";
    }
    
    // 按分类查看商品 - 父分类
    @GetMapping("/category/{parentId}")
    public String viewCategoryProducts(
            @PathVariable Integer parentId,
            Model model, 
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        
        List<CategoryParent> categories = categoryService.getAllParentCategories();
        CategoryParent parent = categoryService.getParentCategoryById(parentId);
        
        // 获取该父分类下所有产品
        List<Product> products = parent.getChildren().stream()
                .flatMap(child -> productService.getProductsByCategoryChildId(child.getId()).stream())
                .distinct()
                .toList();
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedParentId", parentId);
        model.addAttribute("user", user);
        return "user_product_list";
    }
    
    // 按分类查看商品 - 子分类
    @GetMapping("/category/{parentId}/{childId}")
    public String viewSubCategoryProducts(
            @PathVariable Integer parentId,
            @PathVariable Integer childId,
            Model model, 
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        
        List<CategoryParent> categories = categoryService.getAllParentCategories();
        List<Product> products = productService.getProductsByCategoryChildId(childId);
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedParentId", parentId);
        model.addAttribute("selectedChildId", childId);
        model.addAttribute("user", user);
        return "user_product_list";
    }
    
    // 产品详情
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Integer id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/user/index";
        }
        
        model.addAttribute("product", product);
        model.addAttribute("user", user);
        return "product_detail";
    }
    
    // 商品搜索
    @GetMapping("/search")
    public String searchProducts(@RequestParam(required = false) String keyword, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/user/login";
        
        List<Product> products = productService.searchProducts(keyword);
        List<CategoryParent> categories = categoryService.getAllParentCategories();
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("user", user);
        return "user_product_list";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }

    private Integer tryParseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }
} 