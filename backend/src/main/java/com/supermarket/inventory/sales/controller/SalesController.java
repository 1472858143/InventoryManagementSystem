package com.supermarket.inventory.sales.controller;

import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.sales.dto.SalesOrderCreateRequest;
import com.supermarket.inventory.sales.service.SalesService;
import com.supermarket.inventory.sales.vo.SalesOrderDetailResponse;
import com.supermarket.inventory.sales.vo.SalesOrderSummaryResponse;
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
@RequestMapping("/api/sales/orders")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping
    public ApiResponse<SalesOrderDetailResponse> create(@RequestBody SalesOrderCreateRequest request) {
        return ApiResponse.success(salesService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<SalesOrderSummaryResponse>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long customerId,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(salesService.list(new OrderQueryRequest(
            keyword, customerId, startDate, endDate, page, pageSize
        )));
    }

    @GetMapping("/{id}")
    public ApiResponse<SalesOrderDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(salesService.detail(id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @RequestParam(required = false) Long operatorId) {
        salesService.cancel(id, operatorId);
        return ApiResponse.success();
    }
}
