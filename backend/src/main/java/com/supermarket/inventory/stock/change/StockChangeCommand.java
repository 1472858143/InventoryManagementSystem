package com.supermarket.inventory.stock.change;

public record StockChangeCommand(
    Long productId,
    Integer delta,
    SourceType sourceType,
    Long sourceId,
    Long operatorId,
    String reason
) {
}
