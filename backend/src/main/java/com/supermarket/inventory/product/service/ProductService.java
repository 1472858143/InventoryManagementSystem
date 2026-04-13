package com.supermarket.inventory.product.service;

import com.supermarket.inventory.product.dto.ProductCreateRequest;
import com.supermarket.inventory.product.dto.ProductStatusUpdateRequest;
import com.supermarket.inventory.product.vo.ProductDetailResponse;
import com.supermarket.inventory.product.vo.ProductListItemResponse;

import java.util.List;

public interface ProductService {

    ProductDetailResponse createProduct(ProductCreateRequest request);

    List<ProductListItemResponse> listProducts();

    void updateProductStatus(ProductStatusUpdateRequest request);
}
