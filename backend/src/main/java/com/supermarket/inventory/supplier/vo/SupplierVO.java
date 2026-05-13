package com.supermarket.inventory.supplier.vo;

import java.time.LocalDateTime;

public record SupplierVO(
    Long id,
    String code,
    String name,
    String contactPerson,
    String phone,
    String address,
    String remark,
    Integer status,
    LocalDateTime createdAt
) {}
