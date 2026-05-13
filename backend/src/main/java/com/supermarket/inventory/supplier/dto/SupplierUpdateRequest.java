package com.supermarket.inventory.supplier.dto;

import jakarta.validation.constraints.NotBlank;

public record SupplierUpdateRequest(
    @NotBlank(message = "供应商名称不能为空") String name,
    String contactPerson,
    String phone,
    String address,
    String remark
) {}
