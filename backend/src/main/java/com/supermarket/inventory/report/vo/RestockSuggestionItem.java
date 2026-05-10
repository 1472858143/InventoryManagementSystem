package com.supermarket.inventory.report.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestockSuggestionItem {
    private Long productId;
    private String productCode;
    private String productName;
    private Integer quantity;
    private Integer minStock;
    private String shelfStatus;
}
