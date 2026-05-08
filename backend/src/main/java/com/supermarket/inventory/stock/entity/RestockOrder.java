package com.supermarket.inventory.stock.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RestockOrder {
    private Long id;
    private Long productId;
    private Integer quantity;
    private String operator;
    private LocalDateTime createTime;
}
