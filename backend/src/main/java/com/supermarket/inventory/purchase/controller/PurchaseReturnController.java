package com.supermarket.inventory.purchase.controller;

import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.purchase.dto.PurchaseReturnCreateRequest;
import com.supermarket.inventory.purchase.service.PurchaseReturnService;
import com.supermarket.inventory.purchase.vo.PurchaseReturnDetailResponse;
import com.supermarket.inventory.purchase.vo.PurchaseReturnSummaryResponse;
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
@RequestMapping("/api/purchase/returns")
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    public PurchaseReturnController(PurchaseReturnService purchaseReturnService) {
        this.purchaseReturnService = purchaseReturnService;
    }

    @PostMapping
    public ApiResponse<PurchaseReturnDetailResponse> create(@RequestBody PurchaseReturnCreateRequest request) {
        return ApiResponse.success(purchaseReturnService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<PurchaseReturnSummaryResponse>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long supplierId,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(purchaseReturnService.list(new OrderQueryRequest(
            keyword, supplierId, startDate, endDate, page, pageSize
        )));
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseReturnDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(purchaseReturnService.detail(id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @RequestParam(required = false) Long operatorId) {
        purchaseReturnService.cancel(id, operatorId);
        return ApiResponse.success();
    }
}
