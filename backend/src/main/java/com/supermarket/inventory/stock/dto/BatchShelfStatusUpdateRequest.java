package com.supermarket.inventory.stock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchShelfStatusUpdateRequest(
    @NotEmpty(message = "商品ID列表不能为空")
    List<Long> productIds,
    @NotBlank(message = "陈列状态不能为空")
    String shelfStatus
) {}
