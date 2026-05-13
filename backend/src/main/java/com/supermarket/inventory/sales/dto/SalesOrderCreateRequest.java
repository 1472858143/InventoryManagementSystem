package com.supermarket.inventory.sales.dto;

import java.util.List;

public record SalesOrderCreateRequest(
    Long customerId,
    Long operatorId,
    String remark,
    List<SalesOrderItemRequest> items
) {
}
