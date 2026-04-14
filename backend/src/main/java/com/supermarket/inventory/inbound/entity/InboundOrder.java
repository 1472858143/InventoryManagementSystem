package com.supermarket.inventory.inbound.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class InboundOrder {

    private Long id;
    private Long productId;
    private Integer quantity;
    private String operator;
    private LocalDateTime createTime;

}
