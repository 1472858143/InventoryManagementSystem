package com.supermarket.inventory.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerUpdateRequest(
    @NotBlank(message = "客户名称不能为空") String name,
    String contactPerson,
    String phone,
    String address,
    String remark
) {}
