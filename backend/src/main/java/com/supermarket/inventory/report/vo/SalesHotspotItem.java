package com.supermarket.inventory.report.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesHotspotItem {
    private Long productId;
    private String productCode;
    private String productName;
    private Long totalOutbound;
}
