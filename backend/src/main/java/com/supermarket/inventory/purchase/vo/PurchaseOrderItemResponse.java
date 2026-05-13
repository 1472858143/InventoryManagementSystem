package com.supermarket.inventory.purchase.vo;

import java.math.BigDecimal;

public record PurchaseOrderItemResponse(
    Long id,
    Long productId,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal,
    String remark
) {
}
