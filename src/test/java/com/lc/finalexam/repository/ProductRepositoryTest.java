package com.lc.finalexam.repository;

import com.lc.finalexam.dto.ProductQuery;
import com.lc.finalexam.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void setUp() {
        repository.saveAll(List.of(
                product("WM-001", "Drum Washer", "A1 Warehouse", Product.STATUS_ACTIVE),
                product("WM-002", "Top Load Washer", "B2 Warehouse", Product.STATUS_OUT_OF_STOCK),
                product("WM-003", "Mini Washer", "A1 Warehouse", Product.STATUS_INACTIVE)
        ));
    }

    @Test
    void visibleQueryExcludesInactiveAndSortsByNewestIdFirst() {
        Page<Product> result = repository.findAll(
                ProductSpecifications.visibleWith(new ProductQuery()),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(result.getContent())
                .extracting(Product::getProductCode)
                .containsExactly("WM-002", "WM-001");
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void productCodeUsesTrimmedExactMatch() {
        ProductQuery query = new ProductQuery();
        query.setProductCode("  WM-001 ");

        Page<Product> result = repository.findAll(
                ProductSpecifications.visibleWith(query), PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Product::getProductCode)
                .containsExactly("WM-001");
    }

    @Test
    void nameAndStockLocationUseCaseInsensitiveContainsWithAndSemantics() {
        ProductQuery query = new ProductQuery();
        query.setName("DRUM");
        query.setStockLocation("a1");

        Page<Product> result = repository.findAll(
                ProductSpecifications.visibleWith(query), PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Product::getProductCode)
                .containsExactly("WM-001");
    }

    @Test
    void blankFiltersAreIgnoredAndPaginationReportsTotals() {
        ProductQuery query = new ProductQuery();
        query.setName("   ");

        Page<Product> result = repository.findAll(
                ProductSpecifications.visibleWith(query),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(result.getContent())
                .extracting(Product::getProductCode)
                .containsExactly("WM-002");
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    private Product product(String code, String name, String location, String status) {
        Product product = new Product();
        product.setProductCode(code);
        product.setName(name);
        product.setStockLocation(location);
        product.setStatus(status);
        return product;
    }
}
