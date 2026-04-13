package com.supermarket.inventory.product.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class Product {

    private Long id;
    private String productCode;
    private String productName;
    private String category;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Integer status;
    private LocalDateTime createTime;

}
