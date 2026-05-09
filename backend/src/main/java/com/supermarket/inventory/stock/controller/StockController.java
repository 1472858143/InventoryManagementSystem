package com.supermarket.inventory.stock.controller;

import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.stock.dto.BatchShelfStatusUpdateRequest;
import com.supermarket.inventory.stock.dto.ShelfStatusUpdateRequest;
import com.supermarket.inventory.stock.dto.StockLimitUpdateRequest;
import com.supermarket.inventory.stock.service.StockService;
import com.supermarket.inventory.stock.vo.StockDetailResponse;
import com.supermarket.inventory.stock.vo.StockListItemResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public ApiResponse<List<StockListItemResponse>> listStocks() {
        return ApiResponse.success(stockService.listStocks());
    }

    @GetMapping("/{productId}")
    public ApiResponse<StockDetailResponse> getStockByProductId(@PathVariable Long productId) {
        return ApiResponse.success(stockService.getStockByProductId(productId));
    }

    @PutMapping("/{productId}/limit")
    public ApiResponse<Void> updateStockLimit(
        @PathVariable Long productId,
        @Valid @RequestBody StockLimitUpdateRequest request
    ) {
        stockService.updateStockLimit(productId, request);
        return ApiResponse.success();
    }

    @PatchMapping("/{productId}/shelf-status")
    public ApiResponse<Void> updateShelfStatus(
        @PathVariable Long productId,
        @Valid @RequestBody ShelfStatusUpdateRequest request
    ) {
        stockService.updateShelfStatus(productId, request.shelfStatus());
        return ApiResponse.success();
    }

    @PostMapping("/shelf-status/batch")
    public ApiResponse<Void> batchUpdateShelfStatus(
        @Valid @RequestBody BatchShelfStatusUpdateRequest request
    ) {
        stockService.batchUpdateShelfStatus(request.productIds(), request.shelfStatus());
        return ApiResponse.success();
    }
}
