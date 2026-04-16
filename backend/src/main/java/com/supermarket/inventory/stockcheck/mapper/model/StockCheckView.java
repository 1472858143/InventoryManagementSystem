package com.supermarket.inventory.stockcheck.mapper.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class StockCheckView {

    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Integer systemQuantity;
    private Integer actualQuantity;
    private Integer difference;
    private LocalDateTime checkTime;

}
