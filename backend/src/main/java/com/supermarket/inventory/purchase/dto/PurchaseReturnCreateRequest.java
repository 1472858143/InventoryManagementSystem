package com.supermarket.inventory.purchase.dto;

import java.util.List;

public record PurchaseReturnCreateRequest(
    Long supplierId,
    Long sourceOrderId,
    Long operatorId,
    String reason,
    List<PurchaseReturnItemRequest> items
) {
}
