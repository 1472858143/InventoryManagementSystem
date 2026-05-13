package com.supermarket.inventory.sales.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SalesOrderSummaryResponse(
    Long id,
    String orderNo,
    Long customerId,
    Long operatorId,
    Integer totalQuantity,
    BigDecimal totalAmount,
    String status,
    String remark,
    LocalDateTime createdAt
) {
}
