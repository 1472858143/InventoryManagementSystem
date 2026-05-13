package com.supermarket.inventory.purchase.mapper;

import com.supermarket.inventory.purchase.entity.PurchaseOrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseOrderItemMapper {

    @Insert("""
        INSERT INTO purchase_order_item (
            order_id, product_id, quantity, unit_price, subtotal, remark
        ) VALUES (
            #{orderId}, #{productId}, #{quantity}, #{unitPrice}, #{subtotal}, #{remark}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PurchaseOrderItem item);

    @Select("""
        SELECT id AS id, order_id AS orderId, product_id AS productId,
               quantity AS quantity, unit_price AS unitPrice,
               subtotal AS subtotal, remark AS remark
        FROM purchase_order_item
        WHERE order_id = #{orderId}
        ORDER BY id ASC
        """)
    List<PurchaseOrderItem> findByOrderId(@Param("orderId") Long orderId);
}
