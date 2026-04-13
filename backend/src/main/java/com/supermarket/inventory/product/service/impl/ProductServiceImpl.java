package com.supermarket.inventory.product.service.impl;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.product.dto.ProductCreateRequest;
import com.supermarket.inventory.product.dto.ProductStatusUpdateRequest;
import com.supermarket.inventory.product.entity.Product;
import com.supermarket.inventory.product.mapper.ProductMapper;
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

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        Product existingProduct = productMapper.findByProductCode(request.productCode());
        if (existingProduct != null) {
            throw new BusinessException(400, "商品编码已存在");
        }

        validateCreateRequest(request);
        validatePriceRules(request.purchasePrice(), request.salePrice());

        Product product = new Product();
        product.setProductCode(request.productCode());
        product.setProductName(request.productName());
        product.setCategory(request.category());
        product.setPurchasePrice(request.purchasePrice());
        product.setSalePrice(request.salePrice());
        product.setStatus(DEFAULT_PRODUCT_STATUS);

        productMapper.insert(product);

        Product createdProduct = productMapper.findById(product.getId());
        return toProductDetailResponse(createdProduct != null ? createdProduct : product);
    }

    @Override
    public List<ProductListItemResponse> listProducts() {
        List<Product> products = productMapper.findAll();
        if (products.isEmpty()) {
            return List.of();
        }

        List<ProductListItemResponse> responses = new ArrayList<>(products.size());
        for (Product product : products) {
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
        if (isBlank(request.productName())) {
            throw new BusinessException(400, "商品名称不能为空");
        }
        if (isBlank(request.category())) {
            throw new BusinessException(400, "商品分类不能为空");
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

    private ProductDetailResponse toProductDetailResponse(Product product) {
        return new ProductDetailResponse(
            product.getId(),
            product.getProductCode(),
            product.getProductName(),
            product.getCategory(),
            product.getPurchasePrice(),
            product.getSalePrice(),
            product.getStatus(),
            resolveCreateTime(product)
        );
    }

    private ProductListItemResponse toProductListItemResponse(Product product) {
        return new ProductListItemResponse(
            product.getId(),
            product.getProductCode(),
            product.getProductName(),
            product.getCategory(),
            product.getPurchasePrice(),
            product.getSalePrice(),
            product.getStatus(),
            product.getCreateTime()
        );
    }

    private LocalDateTime resolveCreateTime(Product product) {
        if (product.getCreateTime() != null) {
            return product.getCreateTime();
        }
        return LocalDateTime.now();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
