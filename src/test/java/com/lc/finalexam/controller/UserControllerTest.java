package com.lc.finalexam.controller;

import com.lc.finalexam.entity.Product;
import com.lc.finalexam.entity.User;
import com.lc.finalexam.service.CategoryService;
import com.lc.finalexam.service.ProductService;
import com.lc.finalexam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private MessageSource messageSource;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(
                userService, productService, categoryService, messageSource);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void userIndexRendersVisibleProductsReturnedByService() throws Exception {
        Product active = new Product();
        active.setProductCode("WM-001");
        active.setStatus(Product.STATUS_ACTIVE);
        Product outOfStock = new Product();
        outOfStock.setProductCode("WM-002");
        outOfStock.setStatus(Product.STATUS_OUT_OF_STOCK);
        List<Product> visibleProducts = List.of(active, outOfStock);
        when(productService.getAllProducts()).thenReturn(visibleProducts);
        when(categoryService.getAllParentCategories()).thenReturn(List.of());

        mockMvc.perform(get("/user/index")
                        .sessionAttr("user", new User()))
                .andExpect(status().isOk())
                .andExpect(view().name("user_product_list"))
                .andExpect(model().attribute("products", visibleProducts));

        verify(productService).getAllProducts();
    }
}
