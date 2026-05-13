package com.supermarket.inventory.sales.vo;

import java.math.BigDecimal;

public record SalesReturnItemResponse(
    Long id,
    Long productId,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal,
    String remark
) {
}
