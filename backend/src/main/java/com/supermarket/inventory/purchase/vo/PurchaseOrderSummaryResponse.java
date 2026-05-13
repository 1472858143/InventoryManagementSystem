package com.supermarket.inventory.purchase.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseOrderSummaryResponse(
    Long id,
    String orderNo,
    Long supplierId,
    Long operatorId,
    Integer totalQuantity,
    BigDecimal totalAmount,
    String status,
    String remark,
    LocalDateTime createdAt
) {
}
