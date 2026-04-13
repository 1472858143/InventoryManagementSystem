package com.supermarket.inventory.stock.domain;

import com.supermarket.inventory.stock.enums.StockChangeTypeEnum;

public record StockChangeCommand(
    Long productId,
    StockChangeTypeEnum changeType,
    Integer changeQuantity
) {
}
