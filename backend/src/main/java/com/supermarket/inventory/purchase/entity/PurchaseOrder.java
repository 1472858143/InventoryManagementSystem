package com.supermarket.inventory.purchase.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PurchaseOrder {
    private Long id;
    private String orderNo;
    private Long supplierId;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private Long operatorId;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
