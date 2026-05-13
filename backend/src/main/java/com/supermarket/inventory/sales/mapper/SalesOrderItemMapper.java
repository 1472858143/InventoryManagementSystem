package com.supermarket.inventory.sales.mapper;

import com.supermarket.inventory.sales.entity.SalesOrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SalesOrderItemMapper {

    @Insert("""
        INSERT INTO sales_order_item (
            order_id, product_id, quantity, unit_price, subtotal, remark
        ) VALUES (
            #{orderId}, #{productId}, #{quantity}, #{unitPrice}, #{subtotal}, #{remark}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SalesOrderItem item);

    @Select("""
        SELECT id AS id, order_id AS orderId, product_id AS productId,
               quantity AS quantity, unit_price AS unitPrice,
               subtotal AS subtotal, remark AS remark
        FROM sales_order_item
        WHERE order_id = #{orderId}
        ORDER BY id ASC
        """)
    List<SalesOrderItem> findByOrderId(@Param("orderId") Long orderId);
}
