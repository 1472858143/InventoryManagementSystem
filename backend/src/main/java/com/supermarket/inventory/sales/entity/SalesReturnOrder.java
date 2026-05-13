package com.supermarket.inventory.sales.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SalesReturnOrder {
    private Long id;
    private String returnNo;
    private Long customerId;
    private Long sourceOrderId;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private Long operatorId;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
