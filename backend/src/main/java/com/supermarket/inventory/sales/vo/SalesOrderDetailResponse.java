package com.supermarket.inventory.sales.vo;

import java.math.BigDecimal;
import java.util.List;

public record SalesOrderDetailResponse(
    Long id,
    String orderNo,
    Long customerId,
    Long operatorId,
    Integer totalQuantity,
    BigDecimal totalAmount,
    String status,
    String remark,
    List<SalesOrderItemResponse> items
) {
}
