package com.supermarket.inventory.category.service.impl;

import com.supermarket.inventory.category.entity.Category;
import com.supermarket.inventory.category.mapper.CategoryMapper;
import com.supermarket.inventory.category.service.CategoryService;
import com.supermarket.inventory.category.vo.CategoryListItemResponse;
import com.supermarket.inventory.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryListItemResponse> listCategories() {
        return categoryMapper.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<CategoryListItemResponse> listEnabledCategories() {
        return categoryMapper.findAllEnabled().stream().map(this::toResponse).toList();
    }

    @Override
    public CategoryListItemResponse createCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new BusinessException(400, "分类名称不能为空");
        }
        Category category = new Category();
        category.setCategoryName(categoryName.trim());
        categoryMapper.insert(category);
        return toResponse(categoryMapper.findById(category.getId()));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (id == null) throw new BusinessException(400, "分类ID不能为空");
        if (status == null || (status != 0 && status != 1)) throw new BusinessException(400, "状态值非法");
        if (categoryMapper.updateStatusById(id, status) == 0) throw new BusinessException(404, "分类不存在");
    }

    private CategoryListItemResponse toResponse(Category c) {
        return new CategoryListItemResponse(c.getId(), c.getCategoryName(), c.getStatus(), c.getCreateTime());
    }
}
