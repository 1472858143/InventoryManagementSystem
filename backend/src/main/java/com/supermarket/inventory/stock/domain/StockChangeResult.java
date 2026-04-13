package com.supermarket.inventory.stock.domain;

import com.supermarket.inventory.stock.enums.StockChangeTypeEnum;

public record StockChangeResult(
    Long productId,
    StockChangeTypeEnum changeType,
    Integer changeQuantity,
    Integer beforeQuantity,
    Integer afterQuantity
) {
}
