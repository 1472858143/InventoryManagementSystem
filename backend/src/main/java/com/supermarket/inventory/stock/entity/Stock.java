package com.supermarket.inventory.stock.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Stock {
    private Long id;
    private Long productId;
    private Integer quantity;
    private String shelfStatus;
    private Integer minStock;
    private Integer maxStock;
    private LocalDateTime updateTime;
}
