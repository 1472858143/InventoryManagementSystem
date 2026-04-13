package com.supermarket.inventory.stock.mapper;

import com.supermarket.inventory.stock.entity.Stock;
import com.supermarket.inventory.stock.model.StockView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockMapper {

    List<StockView> findAll();

    StockView findByProductId(@Param("productId") Long productId);

    Stock findEntityByProductId(@Param("productId") Long productId);

    int updateLimitByProductId(
        @Param("productId") Long productId,
        @Param("minStock") Integer minStock,
        @Param("maxStock") Integer maxStock
    );

    int updateQuantityByProductId(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
