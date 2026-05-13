package com.supermarket.inventory.purchase.dto;

import java.math.BigDecimal;

public record PurchaseOrderItemRequest(
    Long productId,
    Integer quantity,
    BigDecimal unitPrice,
    String remark
) {
}
