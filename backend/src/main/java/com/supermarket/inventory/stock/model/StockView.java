package com.supermarket.inventory.stock.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StockView {
    private Long productId;
    private String productCode;
    private String productName;
    private String unit;
    private Integer warehouseQuantity;
    private Integer shelfQuantity;
    private Integer minStock;
    private Integer maxStock;
    private LocalDateTime updateTime;
}
