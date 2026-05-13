package com.supermarket.inventory.purchase.vo;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseOrderDetailResponse(
    Long id,
    String orderNo,
    Long supplierId,
    Long operatorId,
    Integer totalQuantity,
    BigDecimal totalAmount,
    String status,
    String remark,
    List<PurchaseOrderItemResponse> items
) {
}
