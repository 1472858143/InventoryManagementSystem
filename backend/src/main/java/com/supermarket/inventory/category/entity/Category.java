package com.supermarket.inventory.category.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Category {
    private Long id;
    private String categoryName;
    private Integer status;
    private LocalDateTime createTime;
}
