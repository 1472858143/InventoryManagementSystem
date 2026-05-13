package com.supermarket.inventory.sales.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SalesOrder {
    private Long id;
    private String orderNo;
    private Long customerId;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private Long operatorId;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
