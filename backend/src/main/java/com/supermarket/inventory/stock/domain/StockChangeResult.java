package com.supermarket.inventory.stock.domain;

import com.supermarket.inventory.stock.enums.StockChangeTypeEnum;

public record StockChangeResult(
    Long productId,
    StockChangeTypeEnum changeType,
    Integer warehouseChangeQuantity,
    Integer shelfChangeQuantity,
    Integer beforeWarehouseQuantity,
    Integer afterWarehouseQuantity,
    Integer beforeShelfQuantity,
    Integer afterShelfQuantity
) {}
