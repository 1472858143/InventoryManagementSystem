package com.supermarket.inventory.sales.controller;

import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.sales.dto.SalesReturnCreateRequest;
import com.supermarket.inventory.sales.service.SalesReturnService;
import com.supermarket.inventory.sales.vo.SalesReturnDetailResponse;
import com.supermarket.inventory.sales.vo.SalesReturnSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sales/returns")
public class SalesReturnController {

    private final SalesReturnService salesReturnService;

    public SalesReturnController(SalesReturnService salesReturnService) {
        this.salesReturnService = salesReturnService;
    }

    @PostMapping
    public ApiResponse<SalesReturnDetailResponse> create(@RequestBody SalesReturnCreateRequest request) {
        return ApiResponse.success(salesReturnService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<SalesReturnSummaryResponse>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long customerId,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(salesReturnService.list(new OrderQueryRequest(
            keyword, customerId, startDate, endDate, page, pageSize
        )));
    }

    @GetMapping("/{id}")
    public ApiResponse<SalesReturnDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(salesReturnService.detail(id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @RequestParam(required = false) Long operatorId) {
        salesReturnService.cancel(id, operatorId);
        return ApiResponse.success();
    }
}
