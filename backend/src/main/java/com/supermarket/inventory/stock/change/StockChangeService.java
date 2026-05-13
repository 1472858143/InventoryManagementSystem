package com.supermarket.inventory.stock.change;

public interface StockChangeService {

    StockChangeResult apply(StockChangeCommand command);
}
