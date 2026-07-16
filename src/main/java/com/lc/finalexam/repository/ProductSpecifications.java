package com.lc.finalexam.repository;

import com.lc.finalexam.dto.ProductQuery;
import com.lc.finalexam.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> visibleWith(ProductQuery request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("status").in(
                    List.of(Product.STATUS_ACTIVE, Product.STATUS_OUT_OF_STOCK)));

            if (request.normalizedProductCode() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("productCode"), request.normalizedProductCode()));
            }
            if (request.normalizedName() != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        contains(request.normalizedName())));
            }
            if (request.normalizedStockLocation() != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("stockLocation")),
                        contains(request.normalizedStockLocation())));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String contains(String value) {
        return "%" + value.toLowerCase(Locale.ROOT) + "%";
    }
}
