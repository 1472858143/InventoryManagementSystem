package com.supermarket.inventory.stock.vo;

import java.time.LocalDateTime;

public record StockListItemResponse(
    Long productId,
    String productCode,
    String productName,
    String unit,
    Integer quantity,
    String shelfStatus,
    Integer minStock,
    Integer maxStock,
    LocalDateTime updateTime
) {}
