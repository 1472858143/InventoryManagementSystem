package com.supermarket.inventory.product.dto;

import java.math.BigDecimal;

public record ProductCreateRequest(
    // 可选；为空时由后端自动生成商品编号。
    String productCode,
    String productName,
    Long categoryId,
    String unit,
    BigDecimal purchasePrice,
    BigDecimal salePrice
) {}
