package com.supermarket.inventory.stock.dto;

import jakarta.validation.constraints.NotBlank;

public record ShelfStatusUpdateRequest(
    @NotBlank(message = "陈列状态不能为空")
    String shelfStatus
) {}
