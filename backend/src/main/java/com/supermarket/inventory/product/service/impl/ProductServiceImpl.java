package com.supermarket.inventory.product.service.impl;

import com.supermarket.inventory.category.entity.Category;
import com.supermarket.inventory.category.mapper.CategoryMapper;
import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.product.dto.ProductCreateRequest;
import com.supermarket.inventory.product.dto.ProductStatusUpdateRequest;
import com.supermarket.inventory.product.entity.Product;
import com.supermarket.inventory.product.mapper.ProductMapper;
import com.supermarket.inventory.product.model.ProductView;
import com.supermarket.inventory.product.service.ProductService;
import com.supermarket.inventory.product.vo.ProductDetailResponse;
import com.supermarket.inventory.product.vo.ProductListItemResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private static final int DEFAULT_PRODUCT_STATUS = 1;
    private static final String DEFAULT_UNIT = "件";

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    public ProductServiceImpl(ProductMapper productMapper, CategoryMapper categoryMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        Product existingProduct = productMapper.findByProductCode(request.productCode());
        if (existingProduct != null) {
            throw new BusinessException(400, "商品编码已存在");
        }

        validateCreateRequest(request);
        validatePriceRules(request.purchasePrice(), request.salePrice());

        // validate category exists and is enabled
        if (request.categoryId() == null) {
            throw new BusinessException(400, "商品分类不能为空");
        }
        Category category = categoryMapper.findById(request.categoryId());
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException(400, "分类不存在或已禁用");
        }

        Product product = new Product();
        product.setProductCode(request.productCode());
        product.setProductName(request.productName());
        product.setCategoryId(request.categoryId());
        product.setUnit(request.unit() != null && !request.unit().isBlank() ? request.unit() : DEFAULT_UNIT);
        product.setPurchasePrice(request.purchasePrice());
        product.setSalePrice(request.salePrice());
        product.setStatus(DEFAULT_PRODUCT_STATUS);

        productMapper.insert(product);

        ProductView createdProduct = productMapper.findByIdWithCategory(product.getId());
        if (createdProduct != null) {
            return toProductDetailResponse(createdProduct);
        }
        // fallback: build from product + category
        return new ProductDetailResponse(
            product.getId(),
            product.getProductCode(),
            product.getProductName(),
            product.getCategoryId(),
            category.getCategoryName(),
            product.getUnit(),
            product.getPurchasePrice(),
            product.getSalePrice(),
            product.getStatus(),
            LocalDateTime.now()
        );
    }

    @Override
    public List<ProductListItemResponse> listProducts() {
        List<ProductView> products = productMapper.findAllWithCategory();
        if (products.isEmpty()) {
            return List.of();
        }
        List<ProductListItemResponse> responses = new ArrayList<>(products.size());
        for (ProductView product : products) {
            responses.add(toProductListItemResponse(product));
        }
        return responses;
    }

    @Override
    public void updateProductStatus(ProductStatusUpdateRequest request) {
        if (request.status() == null || (request.status() != 0 && request.status() != 1)) {
            throw new BusinessException(400, "状态只能为0或1");
        }

        Product product = productMapper.findById(request.productId());
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        productMapper.updateStatusById(request.productId(), request.status());
    }

    private void validateCreateRequest(ProductCreateRequest request) {
        if (isBlank(request.productCode())) {
            throw new BusinessException(400, "商品编码不能为空");
        }
        if (isBlank(request.productName())) {
            throw new BusinessException(400, "商品名称不能为空");
        }
        if (request.purchasePrice() == null) {
            throw new BusinessException(400, "进价不能为空");
        }
        if (request.salePrice() == null) {
            throw new BusinessException(400, "售价不能为空");
        }
    }

    private void validatePriceRules(BigDecimal purchasePrice, BigDecimal salePrice) {
        if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "进价不能小于0");
        }
        if (salePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "售价不能小于0");
        }
        if (salePrice.compareTo(purchasePrice) < 0) {
            throw new BusinessException(400, "售价不能低于进价");
        }
    }

    private ProductDetailResponse toProductDetailResponse(ProductView product) {
        return new ProductDetailResponse(
            product.getId(),
            product.getProductCode(),
            product.getProductName(),
            product.getCategoryId(),
            product.getCategoryName(),
            product.getUnit(),
            product.getPurchasePrice(),
            product.getSalePrice(),
            product.getStatus(),
            product.getCreateTime() != null ? product.getCreateTime() : LocalDateTime.now()
        );
    }

    private ProductListItemResponse toProductListItemResponse(ProductView product) {
        return new ProductListItemResponse(
            product.getId(),
            product.getProductCode(),
            product.getProductName(),
            product.getCategoryId(),
            product.getCategoryName(),
            product.getUnit(),
            product.getPurchasePrice(),
            product.getSalePrice(),
            product.getStatus(),
            product.getCreateTime()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
