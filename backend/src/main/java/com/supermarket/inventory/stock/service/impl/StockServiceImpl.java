package com.supermarket.inventory.stock.service.impl;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.stock.domain.StockDomainService;
import com.supermarket.inventory.stock.dto.StockLimitUpdateRequest;
import com.supermarket.inventory.stock.entity.Stock;
import com.supermarket.inventory.stock.entity.StockLog;
import com.supermarket.inventory.stock.enums.StockChangeTypeEnum;
import com.supermarket.inventory.stock.mapper.StockLogMapper;
import com.supermarket.inventory.stock.mapper.StockMapper;
import com.supermarket.inventory.stock.model.StockView;
import com.supermarket.inventory.stock.service.StockService;
import com.supermarket.inventory.stock.vo.StockDetailResponse;
import com.supermarket.inventory.stock.vo.StockListItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;
    private final StockDomainService stockDomainService;

    public StockServiceImpl(
        StockMapper stockMapper,
        StockLogMapper stockLogMapper,
        StockDomainService stockDomainService
    ) {
        this.stockMapper = stockMapper;
        this.stockLogMapper = stockLogMapper;
        this.stockDomainService = stockDomainService;
    }

    @Override
    public List<StockListItemResponse> listStocks() {
        List<StockView> stocks = stockMapper.findAll();
        if (stocks.isEmpty()) return List.of();
        List<StockListItemResponse> responses = new ArrayList<>(stocks.size());
        for (StockView stock : stocks) responses.add(toListItem(stock));
        return responses;
    }

    @Override
    public StockDetailResponse getStockByProductId(Long productId) {
        requireProductId(productId);
        StockView stock = stockMapper.findByProductId(productId);
        if (stock == null) throw new BusinessException(404, "库存记录不存在");
        return toDetail(stock);
    }

    @Override
    @Transactional
    public void updateStockLimit(Long productId, StockLimitUpdateRequest request) {
        requireProductId(productId);
        requireExistingStock(productId);
        stockDomainService.validateLimit(request.minStock(), request.maxStock());
        int updated = stockMapper.updateLimitByProductId(productId, request.minStock(), request.maxStock());
        if (updated == 0) throw new BusinessException(404, "库存记录不存在");
    }

    @Override
    @Transactional
    public void increaseStock(Long productId, Integer delta) {
        if (delta == null || delta <= 0) throw new BusinessException(400, "入库数量必须大于0");
        Stock stock = requireExistingStock(productId);
        int before = stock.getQuantity();
        int after = before + delta;
        stockMapper.updateQuantityByProductId(productId, after);
        stockLogMapper.insert(buildLog(productId, StockChangeTypeEnum.INBOUND.name(), "QUANTITY", delta, before, after));
    }

    @Override
    @Transactional
    public void decreaseStock(Long productId, Integer delta) {
        if (delta == null || delta <= 0) throw new BusinessException(400, "出库数量必须大于0");
        Stock stock = requireExistingStock(productId);
        int before = stock.getQuantity();
        if (before < delta) throw new BusinessException(400, "库存不足，当前库存：" + before);
        int after = before - delta;
        stockMapper.updateQuantityByProductId(productId, after);
        stockLogMapper.insert(buildLog(productId, StockChangeTypeEnum.OUTBOUND.name(), "QUANTITY", -delta, before, after));
    }

    @Override
    @Transactional
    public void adjustStock(Long productId, Integer actual) {
        if (actual == null || actual < 0) throw new BusinessException(400, "盘点数量不能为负");
        Stock stock = requireExistingStock(productId);
        int before = stock.getQuantity();
        stockMapper.updateQuantityByProductId(productId, actual);
        stockLogMapper.insert(buildLog(productId, StockChangeTypeEnum.CHECK.name(), "QUANTITY", actual - before, before, actual));
    }

    @Override
    @Transactional
    public void updateShelfStatus(Long productId, String shelfStatus) {
        stockDomainService.validateShelfStatus(shelfStatus);
        requireExistingStock(productId);
        stockMapper.updateShelfStatusByProductId(productId, shelfStatus);
        stockLogMapper.insert(buildLog(productId, StockChangeTypeEnum.SHELF_STATUS.name(), "STATUS", 0, 0, 0));
    }

    @Override
    @Transactional
    public void batchUpdateShelfStatus(List<Long> productIds, String shelfStatus) {
        if (productIds == null || productIds.isEmpty()) throw new BusinessException(400, "商品ID列表不能为空");
        stockDomainService.validateShelfStatus(shelfStatus);
        stockMapper.batchUpdateShelfStatus(productIds, shelfStatus);
        for (Long productId : productIds) {
            stockLogMapper.insert(buildLog(productId, StockChangeTypeEnum.SHELF_STATUS.name(), "STATUS", 0, 0, 0));
        }
    }

    private void requireProductId(Long productId) {
        if (productId == null) throw new BusinessException(400, "商品ID不能为空");
    }

    private Stock requireExistingStock(Long productId) {
        requireProductId(productId);
        Stock stock = stockMapper.findEntityByProductId(productId);
        if (stock == null) throw new BusinessException(404, "库存记录不存在");
        return stock;
    }

    private StockLog buildLog(Long productId, String changeType, String stockType,
                               Integer changeQuantity, Integer before, Integer after) {
        StockLog log = new StockLog();
        log.setProductId(productId);
        log.setChangeType(changeType);
        log.setStockType(stockType);
        log.setChangeQuantity(changeQuantity);
        log.setBeforeQuantity(before);
        log.setAfterQuantity(after);
        return log;
    }

    private StockListItemResponse toListItem(StockView s) {
        return new StockListItemResponse(
            s.getProductId(), s.getProductCode(), s.getProductName(), s.getUnit(),
            s.getQuantity(), s.getShelfStatus(), s.getMinStock(), s.getMaxStock(), s.getUpdateTime()
        );
    }

    private StockDetailResponse toDetail(StockView s) {
        return new StockDetailResponse(
            s.getProductId(), s.getProductCode(), s.getProductName(), s.getUnit(),
            s.getQuantity(), s.getShelfStatus(), s.getMinStock(), s.getMaxStock(), s.getUpdateTime()
        );
    }
}
