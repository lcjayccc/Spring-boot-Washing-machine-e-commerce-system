package com.lc.finalexam.controller;

import com.lc.finalexam.dto.ProductQuery;
import com.lc.finalexam.entity.Product;
import com.lc.finalexam.entity.User;
import com.lc.finalexam.service.CategoryService;
import com.lc.finalexam.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(productService, categoryService))
                .setValidator(validator)
                .build();
    }

    @Test
    void listBindsFiltersAndOneBasedPaginationForAuthenticatedAdmin() throws Exception {
        when(productService.queryVisibleProducts(any(ProductQuery.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 5), 7));

        mockMvc.perform(get("/product/list")
                        .sessionAttr("admin", new User())
                        .param("productCode", "WM-001")
                        .param("name", "Drum")
                        .param("stockLocation", "A1")
                        .param("pageNum", "2")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("product_list"))
                .andExpect(model().attributeExists("productPage", "query"));

        ArgumentCaptor<ProductQuery> queryCaptor = ArgumentCaptor.forClass(ProductQuery.class);
        verify(productService).queryVisibleProducts(queryCaptor.capture());
        ProductQuery query = queryCaptor.getValue();
        assertThat(query.getProductCode()).isEqualTo("WM-001");
        assertThat(query.getName()).isEqualTo("Drum");
        assertThat(query.getStockLocation()).isEqualTo("A1");
        assertThat(query.getPageNum()).isEqualTo(2);
        assertThat(query.getPageSize()).isEqualTo(5);
    }

    @Test
    void listRejectsPageSizeAboveMaximumWithoutQueryingDatabase() throws Exception {
        mockMvc.perform(get("/product/list")
                        .sessionAttr("admin", new User())
                        .param("pageSize", "101"))
                .andExpect(status().isOk())
                .andExpect(view().name("product_list"))
                .andExpect(model().attributeHasFieldErrors("query", "pageSize"))
                .andExpect(model().attributeExists("productPage"));

        verify(productService, never()).queryVisibleProducts(any());
    }

    @ParameterizedTest
    @CsvSource({"pageNum,0", "pageSize,0"})
    void listRejectsPaginationValuesBelowOne(String parameter, String value) throws Exception {
        mockMvc.perform(get("/product/list")
                        .sessionAttr("admin", new User())
                        .param(parameter, value))
                .andExpect(status().isOk())
                .andExpect(view().name("product_list"))
                .andExpect(model().attributeHasFieldErrors("query", parameter));

        verify(productService, never()).queryVisibleProducts(any());
    }

    @Test
    void listRedirectsUnauthenticatedVisitorToAdminLogin() throws Exception {
        mockMvc.perform(get("/product/list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/unified-login?role=admin"));

        verify(productService, never()).queryVisibleProducts(any());
    }

    @Test
    void offShelfUsesPostAndRedirectsWithSuccessMessage() throws Exception {
        mockMvc.perform(post("/product/1/off-shelf")
                        .sessionAttr("admin", new User()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/list"))
                .andExpect(flash().attribute("successMessage", "商品已下架"));

        verify(productService).offShelf(1);
    }

    @Test
    void offShelfRedirectsUnauthenticatedVisitorWithoutChangingProduct() throws Exception {
        mockMvc.perform(post("/product/1/off-shelf"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/unified-login?role=admin"));

        verify(productService, never()).offShelf(any());
    }

    @Test
    void offShelfReportsMissingProductAsBusinessMessage() throws Exception {
        doThrow(new IllegalArgumentException("商品不存在"))
                .when(productService).offShelf(99);

        mockMvc.perform(post("/product/99/off-shelf")
                        .sessionAttr("admin", new User()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/list"))
                .andExpect(flash().attribute("errorMessage", "商品不存在"));
    }

    @Test
    void legacySearchRedirectsKeywordToNewNameFilter() throws Exception {
        mockMvc.perform(get("/product/search")
                        .param("keyword", "Drum"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/list?name=Drum"));
    }
}
