package com.supermarket.inventory.purchase.service;

import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.purchase.dto.PurchaseReturnCreateRequest;
import com.supermarket.inventory.purchase.vo.PurchaseReturnDetailResponse;
import com.supermarket.inventory.purchase.vo.PurchaseReturnSummaryResponse;

public interface PurchaseReturnService {

    PurchaseReturnDetailResponse create(PurchaseReturnCreateRequest request);

    PageResponse<PurchaseReturnSummaryResponse> list(OrderQueryRequest query);

    PurchaseReturnDetailResponse detail(Long id);

    void cancel(Long id, Long operatorId);
}
