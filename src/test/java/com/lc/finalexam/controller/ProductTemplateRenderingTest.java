package com.lc.finalexam.controller;

import com.lc.finalexam.entity.Product;
import com.lc.finalexam.entity.User;
import com.lc.finalexam.service.CategoryService;
import com.lc.finalexam.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductTemplateRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void managementListRendersNewFieldsAndPostOffShelfForm() throws Exception {
        Product product = new Product();
        product.setId(1);
        product.setProductCode("WM-001");
        product.setName("Drum Washer");
        product.setModel("D-1");
        product.setStockLocation("A1 Warehouse");
        product.setStock(5);
        product.setStatus(Product.STATUS_ACTIVE);
        product.setPrice(BigDecimal.valueOf(1999));
        product.setCategories(List.of());
        when(productService.queryVisibleProducts(any()))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/product/list").sessionAttr("admin", new User()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("WM-001")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("A1 Warehouse")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/product/1/off-shelf")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"post\"")));
    }

    @Test
    void invalidPaginationRendersValidationMessage() throws Exception {
        mockMvc.perform(get("/product/list")
                        .sessionAttr("admin", new User())
                        .param("pageSize", "101"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("分页参数无效")));
    }
}
