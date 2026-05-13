package com.supermarket.inventory.purchase.vo;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseReturnDetailResponse(
    Long id,
    String returnNo,
    Long supplierId,
    Long sourceOrderId,
    Long operatorId,
    Integer totalQuantity,
    BigDecimal totalAmount,
    String status,
    String reason,
    List<PurchaseReturnItemResponse> items
) {
}
