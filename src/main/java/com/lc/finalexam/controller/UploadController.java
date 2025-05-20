package com.lc.finalexam.controller;

import com.lc.finalexam.entity.Product;
import com.lc.finalexam.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

@Controller
public class UploadController {
    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("productId") Integer productId,
                         @RequestParam(value = "categoryId", required = false) Integer categoryId) throws IOException {
        if (file.isEmpty()) return "redirect:/product/list";
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String uploadDir = "E:/Learn/软件架构/大作业/lcwork/finalexam/images/";

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(uploadDir + fileName);
        file.transferTo(dest);

        // 更新商品图片路径
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setImageUrl("/images/" + fileName);
            productRepository.save(product);
        }
        // 上传后重定向回商品列表（带分类id）
        if (categoryId != null) {
            return "redirect:/product/list?childId=" + categoryId;
        } else {
            return "redirect:/product/list";
        }
    }
}