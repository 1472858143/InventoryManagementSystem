package com.supermarket.inventory.stockcheck.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockCheckCreateRequest(
    @NotNull(message = "商品ID不能为空")
    Long productId,
    @NotNull(message = "实际库存不能为空")
    @Min(value = 0, message = "实际库存不能小于0")
    Integer actualQuantity
) {
}
