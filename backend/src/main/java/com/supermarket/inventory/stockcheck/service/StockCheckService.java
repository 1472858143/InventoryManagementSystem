package com.supermarket.inventory.stockcheck.service;

import com.supermarket.inventory.stockcheck.dto.StockCheckCreateRequest;
import com.supermarket.inventory.stockcheck.vo.StockCheckDetailResponse;
import com.supermarket.inventory.stockcheck.vo.StockCheckListItemResponse;

import java.util.List;

public interface StockCheckService {

    StockCheckDetailResponse createStockCheck(StockCheckCreateRequest request);

    List<StockCheckListItemResponse> listStockChecks();
}
