package com.supermarket.inventory.report.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDistributionItem {
    private String categoryName;
    private Long totalQuantity;
    private Long itemCount;
}
