package com.supermarket.inventory.purchase.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PurchaseReturnOrder {
    private Long id;
    private String returnNo;
    private Long supplierId;
    private Long sourceOrderId;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private Long operatorId;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
