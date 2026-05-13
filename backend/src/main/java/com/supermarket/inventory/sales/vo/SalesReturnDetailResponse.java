package com.supermarket.inventory.sales.vo;

import java.math.BigDecimal;
import java.util.List;

public record SalesReturnDetailResponse(
    Long id,
    String returnNo,
    Long customerId,
    Long sourceOrderId,
    Long operatorId,
    Integer totalQuantity,
    BigDecimal totalAmount,
    String status,
    String reason,
    List<SalesReturnItemResponse> items
) {
}
