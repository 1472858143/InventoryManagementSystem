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
import com.supermarket.inventory.stock.service.StockService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProductServiceImpl implements ProductService {

    private static final int DEFAULT_PRODUCT_STATUS = 1;
    private static final String DEFAULT_UNIT = "件";
    private static final DateTimeFormatter PRODUCT_CODE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final StockService stockService;

    public ProductServiceImpl(
        ProductMapper productMapper,
        CategoryMapper categoryMapper,
        StockService stockService
    ) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        validateCreateRequest(request);
        validatePriceRules(request.purchasePrice(), request.salePrice());

        String providedProductCode = normalizeProductCode(request.productCode());
        if (providedProductCode != null) {
            Product existingProduct = productMapper.findByProductCode(providedProductCode);
            if (existingProduct != null) {
                throw new BusinessException(400, "商品编码已存在");
            }
        }

        // validate category exists and is enabled
        if (request.categoryId() == null) {
            throw new BusinessException(400, "商品分类不能为空");
        }
        Category category = categoryMapper.findById(request.categoryId());
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException(400, "分类不存在或已禁用");
        }

        Product product = new Product();
        product.setProductCode(providedProductCode);
        product.setProductName(request.productName());
        product.setCategoryId(request.categoryId());
        product.setUnit(request.unit() != null && !request.unit().isBlank() ? request.unit() : DEFAULT_UNIT);
        product.setPurchasePrice(request.purchasePrice());
        product.setSalePrice(request.salePrice());
        product.setStatus(DEFAULT_PRODUCT_STATUS);

        insertProduct(product, providedProductCode == null);
        stockService.initializeStockForProduct(product.getId());

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
            product.getSalesCount() != null ? product.getSalesCount() : 0,
            product.getStatus(),
            product.getCreateTime()
        );
    }

    private void insertProduct(Product product, boolean autoGenerateProductCode) {
        int maxAttempts = autoGenerateProductCode ? 2 : 1;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (autoGenerateProductCode) {
                product.setProductCode(generateProductCode());
            }
            try {
                productMapper.insert(product);
                return;
            } catch (DuplicateKeyException ex) {
                if (!autoGenerateProductCode) {
                    throw new BusinessException(400, "商品编码已存在");
                }
                if (attempt == maxAttempts) {
                    throw new BusinessException(500, "商品编号生成失败，请重试");
                }
            }
        }
    }

    private String generateProductCode() {
        int randomNumber = ThreadLocalRandom.current().nextInt(10_000);
        return "P" + LocalDateTime.now().format(PRODUCT_CODE_TIME_FORMATTER) + String.format("%04d", randomNumber);
    }

    private String normalizeProductCode(String productCode) {
        if (productCode == null || productCode.isBlank()) {
            return null;
        }
        return productCode.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
