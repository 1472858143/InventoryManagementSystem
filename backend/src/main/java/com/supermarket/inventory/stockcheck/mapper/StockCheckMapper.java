package com.supermarket.inventory.stockcheck.mapper;

import com.supermarket.inventory.stockcheck.entity.StockCheck;
import com.supermarket.inventory.stockcheck.mapper.model.StockCheckView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockCheckMapper {

    int insert(StockCheck stockCheck);

    List<StockCheckView> findAll();

    StockCheckView findById(@Param("id") Long id);
}
