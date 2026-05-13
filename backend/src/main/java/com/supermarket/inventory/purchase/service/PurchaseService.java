package com.supermarket.inventory.purchase.service;

import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.purchase.dto.PurchaseOrderCreateRequest;
import com.supermarket.inventory.purchase.vo.PurchaseOrderDetailResponse;
import com.supermarket.inventory.purchase.vo.PurchaseOrderSummaryResponse;

public interface PurchaseService {

    PurchaseOrderDetailResponse create(PurchaseOrderCreateRequest request);

    PageResponse<PurchaseOrderSummaryResponse> list(OrderQueryRequest query);

    PurchaseOrderDetailResponse detail(Long id);

    void cancel(Long id, Long operatorId);
}
