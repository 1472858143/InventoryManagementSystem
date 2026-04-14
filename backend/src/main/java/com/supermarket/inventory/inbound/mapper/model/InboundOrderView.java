package com.supermarket.inventory.inbound.mapper.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class InboundOrderView {

    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Integer quantity;
    private String operator;
    private LocalDateTime createTime;

}
