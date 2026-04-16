package com.supermarket.inventory.stockcheck.controller;

import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.stockcheck.dto.StockCheckCreateRequest;
import com.supermarket.inventory.stockcheck.service.StockCheckService;
import com.supermarket.inventory.stockcheck.vo.StockCheckDetailResponse;
import com.supermarket.inventory.stockcheck.vo.StockCheckListItemResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stockchecks")
public class StockCheckController {

    private final StockCheckService stockCheckService;

    public StockCheckController(StockCheckService stockCheckService) {
        this.stockCheckService = stockCheckService;
    }

    @PostMapping
    public ApiResponse<StockCheckDetailResponse> createStockCheck(
        @Valid @RequestBody StockCheckCreateRequest request
    ) {
        return ApiResponse.success(stockCheckService.createStockCheck(request));
    }

    @GetMapping
    public ApiResponse<List<StockCheckListItemResponse>> listStockChecks() {
        return ApiResponse.success(stockCheckService.listStockChecks());
    }
}
