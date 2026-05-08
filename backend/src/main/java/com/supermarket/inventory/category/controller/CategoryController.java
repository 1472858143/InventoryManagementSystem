package com.supermarket.inventory.category.controller;

import com.supermarket.inventory.category.dto.CategoryCreateRequest;
import com.supermarket.inventory.category.dto.CategoryStatusUpdateRequest;
import com.supermarket.inventory.category.service.CategoryService;
import com.supermarket.inventory.category.vo.CategoryListItemResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryListItemResponse> listCategories() {
        return categoryService.listCategories();
    }

    @GetMapping("/enabled")
    public List<CategoryListItemResponse> listEnabledCategories() {
        return categoryService.listEnabledCategories();
    }

    @PostMapping
    public CategoryListItemResponse createCategory(@RequestBody CategoryCreateRequest request) {
        return categoryService.createCategory(request.categoryName());
    }

    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestBody CategoryStatusUpdateRequest request) {
        categoryService.updateStatus(id, request.status());
    }
}
