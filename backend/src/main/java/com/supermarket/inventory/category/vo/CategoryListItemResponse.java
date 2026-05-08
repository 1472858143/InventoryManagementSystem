package com.supermarket.inventory.category.vo;

import java.time.LocalDateTime;

public record CategoryListItemResponse(Long id, String categoryName, Integer status, LocalDateTime createTime) {}
