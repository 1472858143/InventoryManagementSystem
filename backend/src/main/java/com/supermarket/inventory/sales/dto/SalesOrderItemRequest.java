package com.supermarket.inventory.sales.dto;

import java.math.BigDecimal;

public record SalesOrderItemRequest(
    Long productId,
    Integer quantity,
    BigDecimal unitPrice,
    String remark
) {
}
