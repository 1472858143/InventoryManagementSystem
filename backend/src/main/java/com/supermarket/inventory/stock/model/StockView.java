package com.supermarket.inventory.stock.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class StockView {

    private Long productId;
    private String productCode;
    private String productName;
    private Integer quantity;
    private Integer minStock;
    private Integer maxStock;
    private LocalDateTime updateTime;

}
