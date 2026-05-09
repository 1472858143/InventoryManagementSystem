package com.supermarket.inventory.report.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockOverviewItem {
    private String productName;
    private Integer quantity;
    private String shelfStatus;
    private Integer minStock;
    private Integer maxStock;
}
