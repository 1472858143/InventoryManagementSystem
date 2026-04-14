package com.supermarket.inventory.inbound.vo;

import java.time.LocalDateTime;

public record InboundListItemResponse(
    Long id,
    Long productId,
    String productCode,
    String productName,
    Integer quantity,
    String operator,
    LocalDateTime createTime
) {
}
