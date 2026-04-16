package com.supermarket.inventory.stockcheck.vo;

import java.time.LocalDateTime;

public record StockCheckDetailResponse(
    Long id,
    Long productId,
    String productCode,
    String productName,
    Integer systemQuantity,
    Integer actualQuantity,
    Integer difference,
    LocalDateTime checkTime
) {
}
