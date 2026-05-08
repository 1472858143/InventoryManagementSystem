package com.supermarket.inventory.category.controller;

import com.supermarket.inventory.category.dto.CategoryCreateRequest;
import com.supermarket.inventory.category.dto.CategoryStatusUpdateRequest;
import com.supermarket.inventory.category.service.CategoryService;
import com.supermarket.inventory.category.vo.CategoryListItemResponse;
import com.supermarket.inventory.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryListItemResponse>> listCategories() {
        return ApiResponse.success(categoryService.listCategories());
    }

    @GetMapping("/enabled")
    public ApiResponse<List<CategoryListItemResponse>> listEnabledCategories() {
        return ApiResponse.success(categoryService.listEnabledCategories());
    }

    @PostMapping
    public ApiResponse<CategoryListItemResponse> createCategory(@RequestBody CategoryCreateRequest request) {
        return ApiResponse.success(categoryService.createCategory(request.categoryName()));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody CategoryStatusUpdateRequest request) {
        categoryService.updateStatus(id, request.status());
        return ApiResponse.success();
    }
}
