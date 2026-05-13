package com.supermarket.inventory.purchase.controller;

import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.purchase.dto.PurchaseOrderCreateRequest;
import com.supermarket.inventory.purchase.service.PurchaseService;
import com.supermarket.inventory.purchase.vo.PurchaseOrderDetailResponse;
import com.supermarket.inventory.purchase.vo.PurchaseOrderSummaryResponse;
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
@RequestMapping("/api/purchase/orders")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    public ApiResponse<PurchaseOrderDetailResponse> create(@RequestBody PurchaseOrderCreateRequest request) {
        return ApiResponse.success(purchaseService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<PurchaseOrderSummaryResponse>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long supplierId,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(purchaseService.list(new OrderQueryRequest(
            keyword, supplierId, startDate, endDate, page, pageSize
        )));
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrderDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(purchaseService.detail(id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @RequestParam(required = false) Long operatorId) {
        purchaseService.cancel(id, operatorId);
        return ApiResponse.success();
    }
}
