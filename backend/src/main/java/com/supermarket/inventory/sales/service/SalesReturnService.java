package com.supermarket.inventory.sales.service;

import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.sales.dto.SalesReturnCreateRequest;
import com.supermarket.inventory.sales.vo.SalesReturnDetailResponse;
import com.supermarket.inventory.sales.vo.SalesReturnSummaryResponse;

public interface SalesReturnService {
    SalesReturnDetailResponse create(SalesReturnCreateRequest request);

    PageResponse<SalesReturnSummaryResponse> list(OrderQueryRequest query);

    SalesReturnDetailResponse detail(Long id);

    void cancel(Long id, Long operatorId);
}
