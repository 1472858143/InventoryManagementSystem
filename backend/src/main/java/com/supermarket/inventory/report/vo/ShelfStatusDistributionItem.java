package com.supermarket.inventory.report.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShelfStatusDistributionItem {
    private String shelfStatus;
    private Long count;
}
