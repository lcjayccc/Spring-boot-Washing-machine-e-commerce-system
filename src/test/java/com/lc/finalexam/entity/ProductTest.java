package com.lc.finalexam.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Test
    void offShelfChangesActiveProductToInactive() {
        Product product = new Product();
        product.setStatus(Product.STATUS_ACTIVE);

        product.offShelf();

        assertThat(product.getStatus()).isEqualTo(Product.STATUS_INACTIVE);
    }

    @Test
    void offShelfChangesOutOfStockProductToInactive() {
        Product product = new Product();
        product.setStatus(Product.STATUS_OUT_OF_STOCK);

        product.offShelf();

        assertThat(product.getStatus()).isEqualTo(Product.STATUS_INACTIVE);
    }

    @Test
    void offShelfIsIdempotent() {
        Product product = new Product();
        product.setStatus(Product.STATUS_INACTIVE);

        product.offShelf();

        assertThat(product.getStatus()).isEqualTo(Product.STATUS_INACTIVE);
    }
}
