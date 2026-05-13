package com.supermarket.inventory.sales.dto;

import java.util.List;

public record SalesReturnCreateRequest(
    Long customerId,
    Long sourceOrderId,
    Long operatorId,
    String reason,
    List<SalesReturnItemRequest> items
) {
}
