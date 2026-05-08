package com.supermarket.inventory.category.service;

import com.supermarket.inventory.category.vo.CategoryListItemResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryListItemResponse> listCategories();
    List<CategoryListItemResponse> listEnabledCategories();
    CategoryListItemResponse createCategory(String categoryName);
    void updateStatus(Long id, Integer status);
}
