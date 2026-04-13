package com.supermarket.inventory.stock.mapper;

import com.supermarket.inventory.stock.entity.Stock;
import com.supermarket.inventory.stock.model.StockView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface StockMapper {

    @Select("""
        SELECT
            s.product_id AS productId,
            p.product_code AS productCode,
            p.product_name AS productName,
            s.quantity AS quantity,
            s.min_stock AS minStock,
            s.max_stock AS maxStock,
            s.update_time AS updateTime
        FROM stock s
        INNER JOIN product p ON p.id = s.product_id
        ORDER BY s.id ASC
        """)
    List<StockView> findAll();

    @Select("""
        SELECT
            s.product_id AS productId,
            p.product_code AS productCode,
            p.product_name AS productName,
            s.quantity AS quantity,
            s.min_stock AS minStock,
            s.max_stock AS maxStock,
            s.update_time AS updateTime
        FROM stock s
        INNER JOIN product p ON p.id = s.product_id
        WHERE s.product_id = #{productId}
        """)
    StockView findByProductId(@Param("productId") Long productId);

    @Select("""
        SELECT
            id AS id,
            product_id AS productId,
            quantity AS quantity,
            min_stock AS minStock,
            max_stock AS maxStock,
            update_time AS updateTime
        FROM stock
        WHERE product_id = #{productId}
        """)
    Stock findEntityByProductId(@Param("productId") Long productId);

    @Update("""
        UPDATE stock
        SET min_stock = #{minStock},
            max_stock = #{maxStock}
        WHERE product_id = #{productId}
        """)
    int updateLimitByProductId(
        @Param("productId") Long productId,
        @Param("minStock") Integer minStock,
        @Param("maxStock") Integer maxStock
    );

    @Update("""
        UPDATE stock
        SET quantity = #{quantity}
        WHERE product_id = #{productId}
        """)
    int updateQuantityByProductId(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
