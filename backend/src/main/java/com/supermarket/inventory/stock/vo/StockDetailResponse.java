package com.supermarket.inventory.stock.vo;

import java.time.LocalDateTime;

public record StockDetailResponse(
    Long productId,
    String productCode,
    String productName,
    String unit,
    Integer warehouseQuantity,
    Integer shelfQuantity,
    Integer minStock,
    Integer maxStock,
    LocalDateTime updateTime
) {}
