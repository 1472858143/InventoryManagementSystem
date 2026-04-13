package com.supermarket.inventory.stock.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Stock {

    private Long id;
    private Long productId;
    private Integer quantity;
    private Integer minStock;
    private Integer maxStock;
    private LocalDateTime updateTime;

}
