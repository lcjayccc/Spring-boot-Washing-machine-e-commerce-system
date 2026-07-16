package com.lc.finalexam.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductQueryTest {

    @Test
    void defaultsToFirstPageWithTenItems() {
        ProductQuery query = new ProductQuery();

        assertThat(query.getPageNum()).isEqualTo(1);
        assertThat(query.getPageSize()).isEqualTo(10);
    }

    @Test
    void normalizedFiltersTrimValuesAndIgnoreBlanks() {
        ProductQuery query = new ProductQuery();
        query.setProductCode("  WM-001  ");
        query.setName("   ");
        query.setStockLocation("  A1仓库 ");

        assertThat(query.normalizedProductCode()).isEqualTo("WM-001");
        assertThat(query.normalizedName()).isNull();
        assertThat(query.normalizedStockLocation()).isEqualTo("A1仓库");
    }
}
