package com.supermarket.inventory.sales.service;

import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.sales.dto.SalesOrderCreateRequest;
import com.supermarket.inventory.sales.vo.SalesOrderDetailResponse;
import com.supermarket.inventory.sales.vo.SalesOrderSummaryResponse;

public interface SalesService {
    SalesOrderDetailResponse create(SalesOrderCreateRequest request);

    PageResponse<SalesOrderSummaryResponse> list(OrderQueryRequest query);

    SalesOrderDetailResponse detail(Long id);

    void cancel(Long id, Long operatorId);
}
