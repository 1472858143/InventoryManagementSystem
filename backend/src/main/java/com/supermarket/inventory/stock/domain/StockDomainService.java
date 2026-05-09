package com.supermarket.inventory.stock.domain;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.stock.entity.Stock;
import com.supermarket.inventory.stock.enums.StockChangeTypeEnum;
import org.springframework.stereotype.Service;

@Service
public class StockDomainService {

    public void validateLimit(Integer minStock, Integer maxStock) {
        if (minStock == null) throw new BusinessException(400, "库存下限不能为空");
        if (maxStock == null) throw new BusinessException(400, "库存上限不能为空");
        if (minStock < 0) throw new BusinessException(400, "库存下限不能小于0");
        if (maxStock < 0) throw new BusinessException(400, "库存上限不能小于0");
        if (maxStock < minStock) throw new BusinessException(400, "库存上限不能低于库存下限");
    }

    public StockChangeResult increase(Stock stock, StockChangeCommand command) {
        int before = requireWarehouseQuantity(stock);
        int qty = requirePositive(command.changeQuantity());
        return new StockChangeResult(command.productId(), command.changeType(),
            qty, 0, before, before + qty, stock.getShelfQuantity(), stock.getShelfQuantity());
    }

    public StockChangeResult decrease(Stock stock, StockChangeCommand command) {
        int before = requireShelfQuantity(stock);
        int qty = requirePositive(command.changeQuantity());
        if (before < qty) throw new BusinessException(400, "上架库存不足，请先补货");
        return new StockChangeResult(command.productId(), command.changeType(),
            0, qty, stock.getWarehouseQuantity(), stock.getWarehouseQuantity(), before, before - qty);
    }

    public StockChangeResult restock(Stock stock, StockChangeCommand command) {
        int warehouse = requireWarehouseQuantity(stock);
        int shelf = requireShelfQuantity(stock);
        int qty = requirePositive(command.changeQuantity());
        if (warehouse < qty) throw new BusinessException(400, "仓库库存不足，请先入库");
        return new StockChangeResult(command.productId(), command.changeType(),
            qty, qty, warehouse, warehouse - qty, shelf, shelf + qty);
    }

    public StockChangeResult adjust(Stock stock, StockChangeCommand command) {
        int warehouse = requireWarehouseQuantity(stock);
        int shelf = requireShelfQuantity(stock);
        int currentTotal = warehouse + shelf;
        int actual = requireNonNegative(command.changeQuantity());
        int diff = actual - currentTotal;
        int newWarehouse, newShelf;
        if (diff >= 0) {
            newWarehouse = warehouse + diff;
            newShelf = shelf;
        } else {
            int deficit = -diff;
            if (warehouse >= deficit) {
                newWarehouse = warehouse - deficit;
                newShelf = shelf;
            } else {
                newShelf = shelf - (deficit - warehouse);
                newWarehouse = 0;
            }
        }
        if (newShelf < 0) throw new BusinessException(400, "实际库存数量非法");
        return new StockChangeResult(command.productId(), command.changeType(),
            Math.abs(newWarehouse - warehouse), Math.abs(newShelf - shelf),
            warehouse, newWarehouse, shelf, newShelf);
    }

    private int requireWarehouseQuantity(Stock stock) {
        if (stock == null || stock.getWarehouseQuantity() == null || stock.getWarehouseQuantity() < 0)
            throw new BusinessException(400, "仓库库存数量非法");
        return stock.getWarehouseQuantity();
    }

    private int requireShelfQuantity(Stock stock) {
        if (stock == null || stock.getShelfQuantity() == null || stock.getShelfQuantity() < 0)
            throw new BusinessException(400, "上架库存数量非法");
        return stock.getShelfQuantity();
    }

    private int requirePositive(Integer qty) {
        if (qty == null || qty <= 0) throw new BusinessException(400, "库存数量必须大于0");
        return qty;
    }

    private int requireNonNegative(Integer qty) {
        if (qty == null || qty < 0) throw new BusinessException(400, "库存数量不能为负");
        return qty;
    }
}
