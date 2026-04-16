package com.supermarket.inventory.outbound.vo;

import java.time.LocalDateTime;

public record OutboundDetailResponse(
    Long id,
    Long productId,
    String productCode,
    String productName,
    Integer quantity,
    String operator,
    LocalDateTime createTime
) {
}
