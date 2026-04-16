package com.supermarket.inventory.stockcheck.service.impl;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.stock.service.StockService;
import com.supermarket.inventory.stock.vo.StockDetailResponse;
import com.supermarket.inventory.stockcheck.dto.StockCheckCreateRequest;
import com.supermarket.inventory.stockcheck.entity.StockCheck;
import com.supermarket.inventory.stockcheck.mapper.StockCheckMapper;
import com.supermarket.inventory.stockcheck.mapper.model.StockCheckView;
import com.supermarket.inventory.stockcheck.service.StockCheckService;
import com.supermarket.inventory.stockcheck.vo.StockCheckDetailResponse;
import com.supermarket.inventory.stockcheck.vo.StockCheckListItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockCheckServiceImpl implements StockCheckService {

    private final StockCheckMapper stockCheckMapper;
    private final StockService stockService;

    public StockCheckServiceImpl(StockCheckMapper stockCheckMapper, StockService stockService) {
        this.stockCheckMapper = stockCheckMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public StockCheckDetailResponse createStockCheck(StockCheckCreateRequest request) {
        validateCreateRequest(request);

        StockDetailResponse stock = stockService.getStockByProductId(request.productId());
        Integer systemQuantity = stock.quantity();
        Integer difference = request.actualQuantity() - systemQuantity;

        StockCheck stockCheck = new StockCheck();
        stockCheck.setProductId(request.productId());
        stockCheck.setSystemQuantity(systemQuantity);
        stockCheck.setActualQuantity(request.actualQuantity());
        stockCheck.setDifference(difference);

        stockCheckMapper.insert(stockCheck);
        stockService.adjustStock(request.productId(), request.actualQuantity());

        StockCheckView createdStockCheck = stockCheckMapper.findById(stockCheck.getId());
        if (createdStockCheck == null) {
            throw new BusinessException(500, "盘点记录回查失败");
        }

        return toStockCheckDetailResponse(createdStockCheck);
    }

    @Override
    public List<StockCheckListItemResponse> listStockChecks() {
        List<StockCheckView> stockChecks = stockCheckMapper.findAll();
        if (stockChecks.isEmpty()) {
            return List.of();
        }

        List<StockCheckListItemResponse> responses = new ArrayList<>(stockChecks.size());
        for (StockCheckView stockCheck : stockChecks) {
            responses.add(toStockCheckListItemResponse(stockCheck));
        }
        return responses;
    }

    private void validateCreateRequest(StockCheckCreateRequest request) {
        if (request.productId() == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }
        if (request.actualQuantity() == null) {
            throw new BusinessException(400, "实际库存不能为空");
        }
        if (request.actualQuantity() < 0) {
            throw new BusinessException(400, "实际库存不能小于0");
        }
    }

    private StockCheckListItemResponse toStockCheckListItemResponse(StockCheckView stockCheck) {
        return new StockCheckListItemResponse(
            stockCheck.getId(),
            stockCheck.getProductId(),
            stockCheck.getProductCode(),
            stockCheck.getProductName(),
            stockCheck.getSystemQuantity(),
            stockCheck.getActualQuantity(),
            stockCheck.getDifference(),
            stockCheck.getCheckTime()
        );
    }

    private StockCheckDetailResponse toStockCheckDetailResponse(StockCheckView stockCheck) {
        return new StockCheckDetailResponse(
            stockCheck.getId(),
            stockCheck.getProductId(),
            stockCheck.getProductCode(),
            stockCheck.getProductName(),
            stockCheck.getSystemQuantity(),
            stockCheck.getActualQuantity(),
            stockCheck.getDifference(),
            stockCheck.getCheckTime()
        );
    }
}
