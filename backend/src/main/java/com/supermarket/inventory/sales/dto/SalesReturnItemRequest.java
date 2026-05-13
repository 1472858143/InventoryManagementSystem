package com.supermarket.inventory.sales.dto;

import java.math.BigDecimal;

public record SalesReturnItemRequest(
    Long productId,
    Integer quantity,
    BigDecimal unitPrice,
    String remark
) {
}
