package com.supermarket.inventory.product.controller;

import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.product.dto.ProductCreateRequest;
import com.supermarket.inventory.product.dto.ProductStatusUpdateRequest;
import com.supermarket.inventory.product.service.ProductService;
import com.supermarket.inventory.product.vo.ProductDetailResponse;
import com.supermarket.inventory.product.vo.ProductListItemResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ApiResponse<ProductDetailResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success(productService.createProduct(request));
    }

    @GetMapping
    public ApiResponse<List<ProductListItemResponse>> listProducts() {
        return ApiResponse.success(productService.listProducts());
    }

    @PutMapping("/status")
    public ApiResponse<Void> updateProductStatus(@Valid @RequestBody ProductStatusUpdateRequest request) {
        productService.updateProductStatus(request);
        return ApiResponse.success();
    }
}
