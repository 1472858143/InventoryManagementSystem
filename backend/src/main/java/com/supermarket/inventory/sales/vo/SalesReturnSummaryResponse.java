package com.supermarket.inventory.sales.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SalesReturnSummaryResponse(
    Long id,
    String returnNo,
    Long customerId,
    Long sourceOrderId,
    Long operatorId,
    Integer totalQuantity,
    BigDecimal totalAmount,
    String status,
    String reason,
    LocalDateTime createdAt
) {
}
