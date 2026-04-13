package com.supermarket.inventory.stock.mapper;

import com.supermarket.inventory.stock.entity.StockLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockLogMapper {

    @Insert("""
        INSERT INTO stock_log (
            product_id,
            change_type,
            change_quantity,
            before_quantity,
            after_quantity
        ) VALUES (
            #{productId},
            #{changeType},
            #{changeQuantity},
            #{beforeQuantity},
            #{afterQuantity}
        )
        """)
    int insert(StockLog stockLog);
}
