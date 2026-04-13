package com.supermarket.inventory.stock.service;

import com.supermarket.inventory.stock.dto.StockLimitUpdateRequest;
import com.supermarket.inventory.stock.vo.StockDetailResponse;
import com.supermarket.inventory.stock.vo.StockListItemResponse;

import java.util.List;

public interface StockService {

    List<StockListItemResponse> listStocks();

    StockDetailResponse getStockByProductId(Long productId);

    void updateStockLimit(Long productId, StockLimitUpdateRequest request);

    void increaseStock(Long productId, Integer quantity);

    void decreaseStock(Long productId, Integer quantity);

    void adjustStock(Long productId, Integer actualQuantity);
}
