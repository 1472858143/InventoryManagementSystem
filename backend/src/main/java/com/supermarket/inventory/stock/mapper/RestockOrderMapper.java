package com.supermarket.inventory.stock.mapper;

import com.supermarket.inventory.stock.entity.RestockOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface RestockOrderMapper {

    @Insert("INSERT INTO restock_order (product_id, quantity, operator) VALUES (#{productId}, #{quantity}, #{operator})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RestockOrder restockOrder);
}
