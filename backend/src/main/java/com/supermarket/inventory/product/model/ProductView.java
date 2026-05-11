package com.supermarket.inventory.product.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProductView {
    private Long id;
    private String productCode;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private String unit;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Integer salesCount;
    private Integer status;
    private LocalDateTime createTime;
}
