package com.supermarket.inventory.purchase.dto;

import java.math.BigDecimal;

public record PurchaseReturnItemRequest(
    Long productId,
    Integer quantity,
    BigDecimal unitPrice,
    String remark
) {
}
