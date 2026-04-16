package com.supermarket.inventory.stockcheck.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class StockCheck {

    private Long id;
    private Long productId;
    private Integer systemQuantity;
    private Integer actualQuantity;
    private Integer difference;
    private LocalDateTime checkTime;

}
