package com.lc.finalexam.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ProductQuery {
    private String productCode;
    private String name;
    private String stockLocation;
    private Integer childId;

    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    @Max(100)
    private Integer pageSize = 10;

    public String normalizedProductCode() {
        return normalize(productCode);
    }

    public String normalizedName() {
        return normalize(name);
    }

    public String normalizedStockLocation() {
        return normalize(stockLocation);
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
