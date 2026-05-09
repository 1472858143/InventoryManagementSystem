package com.supermarket.inventory.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RestockRequest(
    @NotNull(message = "补货数量不能为空")
    @Min(value = 1, message = "补货数量不能小于1")
    Integer quantity,
    @NotBlank(message = "操作人不能为空")
    String operator
) {
}
