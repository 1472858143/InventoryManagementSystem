package com.supermarket.inventory.product.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class Product {
    private Long id;
    private String productCode;
    private String productName;
    private Long categoryId;
    private String unit;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Integer status;
    private LocalDateTime createTime;
}
