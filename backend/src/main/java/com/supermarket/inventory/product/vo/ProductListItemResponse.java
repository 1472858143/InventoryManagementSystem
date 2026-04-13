package com.supermarket.inventory.product.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductListItemResponse(
    Long id,
    String productCode,
    String productName,
    String category,
    BigDecimal purchasePrice,
    BigDecimal salePrice,
    Integer status,
    LocalDateTime createTime
) {
}
