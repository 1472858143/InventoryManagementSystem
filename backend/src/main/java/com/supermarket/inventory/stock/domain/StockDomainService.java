package com.supermarket.inventory.stock.domain;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.stock.entity.Stock;
import org.springframework.stereotype.Service;

@Service
public class StockDomainService {

    public void validateLimit(Integer minStock, Integer maxStock) {
        if (minStock == null) {
            throw new BusinessException(400, "库存下限不能为空");
        }
        if (maxStock == null) {
            throw new BusinessException(400, "库存上限不能为空");
        }
        if (minStock < 0) {
            throw new BusinessException(400, "库存下限不能小于0");
        }
        if (maxStock < 0) {
            throw new BusinessException(400, "库存上限不能小于0");
        }
        if (maxStock < minStock) {
            throw new BusinessException(400, "库存上限不能低于库存下限");
        }
    }

    public StockChangeResult increase(Stock stock, StockChangeCommand command) {
        int beforeQuantity = requireCurrentQuantity(stock);
        int changeQuantity = requirePositiveQuantity(command.changeQuantity());
        int afterQuantity = beforeQuantity + changeQuantity;
        return new StockChangeResult(
            command.productId(),
            command.changeType(),
            changeQuantity,
            beforeQuantity,
            afterQuantity
        );
    }

    public StockChangeResult decrease(Stock stock, StockChangeCommand command) {
        int beforeQuantity = requireCurrentQuantity(stock);
        int changeQuantity = requirePositiveQuantity(command.changeQuantity());
        int afterQuantity = beforeQuantity - changeQuantity;
        if (afterQuantity < 0) {
            throw new BusinessException(400, "库存数量非法");
        }
        return new StockChangeResult(
            command.productId(),
            command.changeType(),
            changeQuantity,
            beforeQuantity,
            afterQuantity
        );
    }

    public StockChangeResult adjust(Stock stock, StockChangeCommand command) {
        int beforeQuantity = requireCurrentQuantity(stock);
        int actualQuantity = requireNonNegativeQuantity(command.changeQuantity());
        int changeQuantity = Math.abs(actualQuantity - beforeQuantity);
        return new StockChangeResult(
            command.productId(),
            command.changeType(),
            changeQuantity,
            beforeQuantity,
            actualQuantity
        );
    }

    private int requireCurrentQuantity(Stock stock) {
        if (stock == null || stock.getQuantity() == null) {
            throw new BusinessException(400, "库存数量非法");
        }
        if (stock.getQuantity() < 0) {
            throw new BusinessException(400, "库存数量非法");
        }
        return stock.getQuantity();
    }

    private int requirePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(400, "库存数量非法");
        }
        return quantity;
    }

    private int requireNonNegativeQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new BusinessException(400, "库存数量非法");
        }
        return quantity;
    }
}
