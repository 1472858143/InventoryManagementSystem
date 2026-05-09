package com.supermarket.inventory.product.dto;

import java.math.BigDecimal;

public record ProductCreateRequest(
    String productCode,
    String productName,
    Long categoryId,
    String unit,
    BigDecimal purchasePrice,
    BigDecimal salePrice
) {}
