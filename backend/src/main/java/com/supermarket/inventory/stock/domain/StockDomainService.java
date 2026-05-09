package com.supermarket.inventory.stock.domain;

import com.supermarket.inventory.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class StockDomainService {

    private static final Set<String> VALID_SHELF_STATUSES = Set.of("未上架", "缺货", "较少", "充足");

    public void validateLimit(Integer minStock, Integer maxStock) {
        if (minStock == null) throw new BusinessException(400, "库存下限不能为空");
        if (maxStock == null) throw new BusinessException(400, "库存上限不能为空");
        if (minStock < 0) throw new BusinessException(400, "库存下限不能小于0");
        if (maxStock < 0) throw new BusinessException(400, "库存上限不能小于0");
        if (maxStock < minStock) throw new BusinessException(400, "库存上限不能低于库存下限");
    }

    public void validateShelfStatus(String shelfStatus) {
        if (shelfStatus == null || !VALID_SHELF_STATUSES.contains(shelfStatus)) {
            throw new BusinessException(400, "无效的陈列状态，允许值：未上架/缺货/较少/充足");
        }
    }
}
