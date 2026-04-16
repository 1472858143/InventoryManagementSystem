package com.supermarket.inventory.outbound.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OutboundCreateRequest(
    @NotNull(message = "商品ID不能为空")
    Long productId,
    @NotNull(message = "出库数量不能为空")
    @Min(value = 1, message = "出库数量必须大于0")
    Integer quantity,
    @NotBlank(message = "操作人不能为空")
    String operator
) {
}
