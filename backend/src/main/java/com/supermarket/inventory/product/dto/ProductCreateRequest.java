package com.supermarket.inventory.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductCreateRequest(
    @NotBlank(message = "商品编码不能为空")
    String productCode,
    @NotBlank(message = "商品名称不能为空")
    String productName,
    @NotBlank(message = "商品分类不能为空")
    String category,
    @NotNull(message = "进价不能为空")
    BigDecimal purchasePrice,
    @NotNull(message = "售价不能为空")
    BigDecimal salePrice
) {
}
