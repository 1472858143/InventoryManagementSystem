package com.supermarket.inventory.report.vo;

public record StockOverviewItem(
    String productName,
    Integer warehouseQuantity,
    Integer shelfQuantity,
    Integer totalQuantity,
    Integer minStock,
    Integer maxStock
) {}
