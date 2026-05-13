package com.supermarket.inventory.stock.change;

public record StockChangeResult(
    Long productId,
    Integer beforeQuantity,
    Integer afterQuantity
) {
}
