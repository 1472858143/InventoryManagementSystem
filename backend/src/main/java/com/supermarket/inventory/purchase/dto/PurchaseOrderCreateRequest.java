package com.supermarket.inventory.purchase.dto;

import java.util.List;

public record PurchaseOrderCreateRequest(
    Long supplierId,
    Long operatorId,
    String remark,
    List<PurchaseOrderItemRequest> items
) {
}
