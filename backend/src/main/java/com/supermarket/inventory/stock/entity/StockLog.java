package com.supermarket.inventory.stock.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StockLog {
    private Long id;
    private Long productId;
    private String changeType;
    private String stockType;
    private Integer changeQuantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private LocalDateTime createTime;
}
