package com.supermarket.inventory.stock.mapper;

import com.supermarket.inventory.stock.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockLogMapper {

    int insert(StockLog stockLog);
}
