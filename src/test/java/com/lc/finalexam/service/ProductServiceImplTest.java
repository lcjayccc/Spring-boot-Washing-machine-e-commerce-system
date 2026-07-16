package com.lc.finalexam.service;

import com.lc.finalexam.dto.ProductQuery;
import com.lc.finalexam.entity.Product;
import com.lc.finalexam.repository.ProductRepository;
import com.lc.finalexam.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(repository);
    }

    @Test
    void queryVisibleProductsConvertsOneBasedPageAndSortsByIdDescending() {
        ProductQuery query = new ProductQuery();
        query.setPageNum(2);
        query.setPageSize(5);
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.queryVisibleProducts(query);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("id").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void offShelfChangesExistingProductState() {
        Product product = new Product();
        product.setStatus(Product.STATUS_ACTIVE);
        when(repository.findById(1)).thenReturn(Optional.of(product));

        service.offShelf(1);

        assertThat(product.getStatus()).isEqualTo(Product.STATUS_INACTIVE);
    }

    @Test
    void offShelfIsIdempotentForInactiveProduct() {
        Product product = new Product();
        product.setStatus(Product.STATUS_INACTIVE);
        when(repository.findById(1)).thenReturn(Optional.of(product));

        service.offShelf(1);

        assertThat(product.getStatus()).isEqualTo(Product.STATUS_INACTIVE);
    }

    @Test
    void offShelfRejectsMissingProduct() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.offShelf(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("商品不存在");
    }

    @Test
    void categoryListingRequestsOnlyVisibleStatuses() {
        service.getProductsByCategoryChildId(7);

        verify(repository).findByCategories_IdAndStatusIn(
                7, List.of(Product.STATUS_ACTIVE, Product.STATUS_OUT_OF_STOCK));
    }

    @Test
    void legacyHomepageAndSearchMethodsUseSpecifications() {
        service.getAllProducts();
        service.searchProducts(" drum ");

        verify(repository, org.mockito.Mockito.times(2))
                .findAll(any(Specification.class), any(Sort.class));
    }
}
