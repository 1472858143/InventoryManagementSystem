package com.supermarket.inventory.purchase.mapper;

import com.supermarket.inventory.purchase.entity.PurchaseReturnOrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseReturnOrderItemMapper {

    @Insert("""
        INSERT INTO purchase_return_order_item (
            return_id, product_id, quantity, unit_price, subtotal, remark
        ) VALUES (
            #{returnId}, #{productId}, #{quantity}, #{unitPrice}, #{subtotal}, #{remark}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PurchaseReturnOrderItem item);

    @Select("""
        SELECT id AS id, return_id AS returnId, product_id AS productId,
               quantity AS quantity, unit_price AS unitPrice,
               subtotal AS subtotal, remark AS remark
        FROM purchase_return_order_item
        WHERE return_id = #{returnId}
        ORDER BY id ASC
        """)
    List<PurchaseReturnOrderItem> findByReturnId(@Param("returnId") Long returnId);
}
