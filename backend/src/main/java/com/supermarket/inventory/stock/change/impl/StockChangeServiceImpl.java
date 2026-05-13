package com.supermarket.inventory.stock.change.impl;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.stock.change.StockChangeCommand;
import com.supermarket.inventory.stock.change.StockChangeResult;
import com.supermarket.inventory.stock.change.StockChangeService;
import com.supermarket.inventory.stock.entity.Stock;
import com.supermarket.inventory.stock.entity.StockLog;
import com.supermarket.inventory.stock.mapper.StockLogMapper;
import com.supermarket.inventory.stock.mapper.StockMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockChangeServiceImpl implements StockChangeService {

    private static final String CHANGE_TYPE_INBOUND = "INBOUND";
    private static final String CHANGE_TYPE_OUTBOUND = "OUTBOUND";
    private static final String STOCK_TYPE_WAREHOUSE = "WAREHOUSE";

    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;

    public StockChangeServiceImpl(StockMapper stockMapper, StockLogMapper stockLogMapper) {
        this.stockMapper = stockMapper;
        this.stockLogMapper = stockLogMapper;
    }

    @Override
    @Transactional
    public StockChangeResult apply(StockChangeCommand command) {
        validate(command);

        Stock stock = stockMapper.findEntityByProductIdForUpdate(command.productId());
        if (stock == null) {
            throw new BusinessException(404, "库存记录不存在");
        }

        int before = stock.getQuantity();
        int after = before + command.delta();
        if (after < 0) {
            throw new BusinessException(400, "库存不足，当前库存：" + before);
        }

        stockMapper.updateQuantityByProductId(command.productId(), after);
        stockLogMapper.insert(buildLog(command, before, after));
        return new StockChangeResult(command.productId(), before, after);
    }

    private void validate(StockChangeCommand command) {
        if (command == null) {
            throw new BusinessException(400, "库存变更命令不能为空");
        }
        if (command.productId() == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }
        if (command.delta() == null) {
            throw new BusinessException(400, "库存变更数量不能为空");
        }
        if (command.delta() == 0) {
            throw new BusinessException(400, "库存变更数量不能为0");
        }
        if (command.sourceType() == null) {
            throw new BusinessException(400, "库存变更来源类型不能为空");
        }
        if (command.sourceId() == null) {
            throw new BusinessException(400, "来源单据ID不能为空");
        }
    }

    private StockLog buildLog(StockChangeCommand command, int before, int after) {
        StockLog log = new StockLog();
        log.setProductId(command.productId());
        log.setChangeType(command.delta() > 0 ? CHANGE_TYPE_INBOUND : CHANGE_TYPE_OUTBOUND);
        log.setStockType(STOCK_TYPE_WAREHOUSE);
        log.setChangeQuantity(command.delta());
        log.setBeforeQuantity(before);
        log.setAfterQuantity(after);
        log.setSourceType(command.sourceType().name());
        log.setSourceId(command.sourceId());
        log.setOperatorId(command.operatorId());
        log.setReason(command.reason());
        return log;
    }
}
