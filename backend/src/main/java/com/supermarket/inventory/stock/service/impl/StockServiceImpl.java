package com.supermarket.inventory.stock.service.impl;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.stock.domain.StockChangeCommand;
import com.supermarket.inventory.stock.domain.StockChangeResult;
import com.supermarket.inventory.stock.domain.StockDomainService;
import com.supermarket.inventory.stock.dto.StockLimitUpdateRequest;
import com.supermarket.inventory.stock.entity.RestockOrder;
import com.supermarket.inventory.stock.entity.Stock;
import com.supermarket.inventory.stock.entity.StockLog;
import com.supermarket.inventory.stock.enums.StockChangeTypeEnum;
import com.supermarket.inventory.stock.mapper.RestockOrderMapper;
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
    private final RestockOrderMapper restockOrderMapper;

    public StockServiceImpl(
        StockMapper stockMapper,
        StockLogMapper stockLogMapper,
        StockDomainService stockDomainService,
        RestockOrderMapper restockOrderMapper
    ) {
        this.stockMapper = stockMapper;
        this.stockLogMapper = stockLogMapper;
        this.stockDomainService = stockDomainService;
        this.restockOrderMapper = restockOrderMapper;
    }

    @Override
    public List<StockListItemResponse> listStocks() {
        List<StockView> stocks = stockMapper.findAll();
        if (stocks.isEmpty()) {
            return List.of();
        }
        List<StockListItemResponse> responses = new ArrayList<>(stocks.size());
        for (StockView stock : stocks) {
            responses.add(toStockListItemResponse(stock));
        }
        return responses;
    }

    @Override
    public StockDetailResponse getStockByProductId(Long productId) {
        requireProductId(productId);
        StockView stock = stockMapper.findByProductId(productId);
        if (stock == null) {
            throw new BusinessException(404, "库存记录不存在");
        }
        return toStockDetailResponse(stock);
    }

    @Override
    public void updateStockLimit(Long productId, StockLimitUpdateRequest request) {
        requireProductId(productId);
        requireExistingStock(productId);
        stockDomainService.validateLimit(request.minStock(), request.maxStock());
        stockMapper.updateLimitByProductId(productId, request.minStock(), request.maxStock());
    }

    @Override
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        Stock stock = requireExistingStock(productId);
        StockChangeResult result = stockDomainService.increase(
            stock,
            new StockChangeCommand(productId, StockChangeTypeEnum.INBOUND, quantity)
        );
        stockMapper.updateQuantitiesByProductId(productId, result.afterWarehouseQuantity(), result.afterShelfQuantity());
        StockLog log = buildLog(productId, StockChangeTypeEnum.INBOUND.name(), "WAREHOUSE",
            result.warehouseChangeQuantity(), result.beforeWarehouseQuantity(), result.afterWarehouseQuantity());
        stockLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        Stock stock = requireExistingStock(productId);
        StockChangeResult result = stockDomainService.decrease(
            stock,
            new StockChangeCommand(productId, StockChangeTypeEnum.OUTBOUND, quantity)
        );
        stockMapper.updateQuantitiesByProductId(productId, result.afterWarehouseQuantity(), result.afterShelfQuantity());
        StockLog log = buildLog(productId, StockChangeTypeEnum.OUTBOUND.name(), "SHELF",
            -result.shelfChangeQuantity(), result.beforeShelfQuantity(), result.afterShelfQuantity());
        stockLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void restockStock(Long productId, Integer quantity, String operator) {
        if (productId == null) throw new BusinessException(400, "商品ID不能为空");
        if (operator == null || operator.isBlank()) throw new BusinessException(400, "操作人不能为空");
        Stock stock = requireExistingStock(productId);
        StockChangeResult result = stockDomainService.restock(
            stock,
            new StockChangeCommand(productId, StockChangeTypeEnum.RESTOCK, quantity)
        );
        stockMapper.updateQuantitiesByProductId(productId, result.afterWarehouseQuantity(), result.afterShelfQuantity());
        // log WAREHOUSE decrease
        StockLog warehouseLog = buildLog(productId, StockChangeTypeEnum.RESTOCK.name(), "WAREHOUSE",
            -result.warehouseChangeQuantity(), result.beforeWarehouseQuantity(), result.afterWarehouseQuantity());
        stockLogMapper.insert(warehouseLog);
        // log SHELF increase
        StockLog shelfLog = buildLog(productId, StockChangeTypeEnum.RESTOCK.name(), "SHELF",
            result.shelfChangeQuantity(), result.beforeShelfQuantity(), result.afterShelfQuantity());
        stockLogMapper.insert(shelfLog);
        // save restock_order
        RestockOrder restockOrder = new RestockOrder();
        restockOrder.setProductId(productId);
        restockOrder.setQuantity(quantity);
        restockOrder.setOperator(operator);
        restockOrderMapper.insert(restockOrder);
    }

    @Override
    @Transactional
    public void adjustStock(Long productId, Integer actualQuantity) {
        Stock stock = requireExistingStock(productId);
        StockChangeResult result = stockDomainService.adjust(
            stock,
            new StockChangeCommand(productId, StockChangeTypeEnum.CHECK, actualQuantity)
        );
        stockMapper.updateQuantitiesByProductId(productId, result.afterWarehouseQuantity(), result.afterShelfQuantity());
        StockLog log = buildLog(productId, StockChangeTypeEnum.CHECK.name(), "WAREHOUSE",
            result.afterWarehouseQuantity() - result.beforeWarehouseQuantity(),
            result.beforeWarehouseQuantity(), result.afterWarehouseQuantity());
        stockLogMapper.insert(log);
    }

    private void requireProductId(Long productId) {
        if (productId == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }
    }

    private Stock requireExistingStock(Long productId) {
        requireProductId(productId);
        Stock stock = stockMapper.findEntityByProductId(productId);
        if (stock == null) {
            throw new BusinessException(404, "库存记录不存在");
        }
        return stock;
    }

    private StockLog buildLog(Long productId, String changeType, String stockType,
                               Integer changeQuantity, Integer beforeQuantity, Integer afterQuantity) {
        StockLog log = new StockLog();
        log.setProductId(productId);
        log.setChangeType(changeType);
        log.setStockType(stockType);
        log.setChangeQuantity(changeQuantity);
        log.setBeforeQuantity(beforeQuantity);
        log.setAfterQuantity(afterQuantity);
        return log;
    }

    private StockListItemResponse toStockListItemResponse(StockView stock) {
        return new StockListItemResponse(
            stock.getProductId(),
            stock.getProductCode(),
            stock.getProductName(),
            stock.getUnit(),
            stock.getWarehouseQuantity(),
            stock.getShelfQuantity(),
            stock.getMinStock(),
            stock.getMaxStock(),
            stock.getUpdateTime()
        );
    }

    private StockDetailResponse toStockDetailResponse(StockView stock) {
        return new StockDetailResponse(
            stock.getProductId(),
            stock.getProductCode(),
            stock.getProductName(),
            stock.getUnit(),
            stock.getWarehouseQuantity(),
            stock.getShelfQuantity(),
            stock.getMinStock(),
            stock.getMaxStock(),
            stock.getUpdateTime()
        );
    }
}
