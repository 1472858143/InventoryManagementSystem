package com.supermarket.inventory.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductStatusUpdateRequest(
    @NotNull(message = "商品ID不能为空")
    Long productId,
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    Integer status
) {
}
