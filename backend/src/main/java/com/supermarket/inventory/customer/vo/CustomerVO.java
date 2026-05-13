package com.supermarket.inventory.customer.vo;

import java.time.LocalDateTime;

public record CustomerVO(
    Long id,
    String code,
    String name,
    String contactPerson,
    String phone,
    String address,
    String remark,
    Integer status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
