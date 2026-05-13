package com.supermarket.inventory.purchase.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseReturnSummaryResponse(
    Long id,
    String returnNo,
    Long supplierId,
    Long sourceOrderId,
    Long operatorId,
    Integer totalQuantity,
    BigDecimal totalAmount,
    String status,
    String reason,
    LocalDateTime createdAt
) {
}
