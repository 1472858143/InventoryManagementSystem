package com.supermarket.inventory.report.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockOverviewItem {
    private String productName;
    private Integer warehouseQuantity;
    private Integer shelfQuantity;
    private Integer totalQuantity;
    private Integer minStock;
    private Integer maxStock;
}
