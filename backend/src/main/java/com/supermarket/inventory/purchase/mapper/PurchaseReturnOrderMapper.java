package com.supermarket.inventory.purchase.mapper;

import com.supermarket.inventory.purchase.entity.PurchaseReturnOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PurchaseReturnOrderMapper {

    @Insert("""
        INSERT INTO purchase_return_order (
            return_no, supplier_id, source_order_id, total_quantity, total_amount,
            operator_id, status, reason
        ) VALUES (
            #{returnNo}, #{supplierId}, #{sourceOrderId}, #{totalQuantity}, #{totalAmount},
            #{operatorId}, #{status}, #{reason}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PurchaseReturnOrder order);

    @Select("""
        SELECT id AS id, return_no AS returnNo, supplier_id AS supplierId,
               source_order_id AS sourceOrderId, total_quantity AS totalQuantity,
               total_amount AS totalAmount, operator_id AS operatorId,
               status AS status, reason AS reason, created_at AS createdAt,
               updated_at AS updatedAt
        FROM purchase_return_order
        WHERE id = #{id}
        """)
    PurchaseReturnOrder findById(@Param("id") Long id);

    @Update("""
        UPDATE purchase_return_order
        SET status = #{status}
        WHERE id = #{id}
        """)
    int updateStatusById(@Param("id") Long id, @Param("status") String status);

    @Select("""
        SELECT COUNT(*)
        FROM purchase_return_order
        WHERE (#{keyword} IS NULL OR return_no LIKE CONCAT('%', #{keyword}, '%'))
          AND (#{subjectId} IS NULL OR supplier_id = #{subjectId})
          AND (#{startDate} IS NULL OR DATE(created_at) >= #{startDate})
          AND (#{endDate} IS NULL OR DATE(created_at) <= #{endDate})
        """)
    long count(@Param("keyword") String keyword,
               @Param("subjectId") Long subjectId,
               @Param("startDate") LocalDate startDate,
               @Param("endDate") LocalDate endDate);

    @Select("""
        SELECT id AS id, return_no AS returnNo, supplier_id AS supplierId,
               source_order_id AS sourceOrderId, total_quantity AS totalQuantity,
               total_amount AS totalAmount, operator_id AS operatorId,
               status AS status, reason AS reason, created_at AS createdAt,
               updated_at AS updatedAt
        FROM purchase_return_order
        WHERE (#{keyword} IS NULL OR return_no LIKE CONCAT('%', #{keyword}, '%'))
          AND (#{subjectId} IS NULL OR supplier_id = #{subjectId})
          AND (#{startDate} IS NULL OR DATE(created_at) >= #{startDate})
          AND (#{endDate} IS NULL OR DATE(created_at) <= #{endDate})
        ORDER BY created_at DESC, id DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """)
    List<PurchaseReturnOrder> findAll(@Param("keyword") String keyword,
                                      @Param("subjectId") Long subjectId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize);
}
