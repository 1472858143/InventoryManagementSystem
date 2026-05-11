package com.supermarket.inventory.product.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductListItemResponse(
    Long id,
    String productCode,
    String productName,
    Long categoryId,
    String categoryName,
    String unit,
    BigDecimal purchasePrice,
    BigDecimal salePrice,
    Integer salesCount,
    Integer status,
    LocalDateTime createTime
) {}
