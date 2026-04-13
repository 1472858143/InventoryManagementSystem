package com.supermarket.inventory.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockLimitUpdateRequest(
    @NotNull(message = "库存下限不能为空")
    @Min(value = 0, message = "库存下限不能小于0")
    Integer minStock,
    @NotNull(message = "库存上限不能为空")
    @Min(value = 0, message = "库存上限不能小于0")
    Integer maxStock
) {
}
