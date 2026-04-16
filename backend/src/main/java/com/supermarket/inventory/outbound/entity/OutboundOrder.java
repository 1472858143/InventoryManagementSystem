package com.supermarket.inventory.outbound.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class OutboundOrder {

    private Long id;
    private Long productId;
    private Integer quantity;
    private String operator;
    private LocalDateTime createTime;

}
