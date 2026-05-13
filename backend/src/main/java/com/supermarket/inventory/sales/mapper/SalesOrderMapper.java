package com.supermarket.inventory.sales.mapper;

import com.supermarket.inventory.sales.entity.SalesOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SalesOrderMapper {

    @Insert("""
        INSERT INTO sales_order (
            order_no, customer_id, total_quantity, total_amount,
            operator_id, status, remark
        ) VALUES (
            #{orderNo}, #{customerId}, #{totalQuantity}, #{totalAmount},
            #{operatorId}, #{status}, #{remark}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SalesOrder salesOrder);

    @Select("""
        SELECT id AS id, order_no AS orderNo, customer_id AS customerId,
               total_quantity AS totalQuantity, total_amount AS totalAmount,
               operator_id AS operatorId, status AS status, remark AS remark,
               created_at AS createdAt, updated_at AS updatedAt
        FROM sales_order
        WHERE id = #{id}
        """)
    SalesOrder findById(@Param("id") Long id);

    @Select("""
        SELECT COUNT(*)
        FROM sales_order
        WHERE (#{keyword} IS NULL OR order_no LIKE CONCAT('%', #{keyword}, '%'))
          AND (#{subjectId} IS NULL OR customer_id = #{subjectId})
          AND (#{startDate} IS NULL OR DATE(created_at) >= #{startDate})
          AND (#{endDate} IS NULL OR DATE(created_at) <= #{endDate})
        """)
    long count(@Param("keyword") String keyword,
               @Param("subjectId") Long subjectId,
               @Param("startDate") LocalDate startDate,
               @Param("endDate") LocalDate endDate);

    @Select("""
        SELECT id AS id, order_no AS orderNo, customer_id AS customerId,
               total_quantity AS totalQuantity, total_amount AS totalAmount,
               operator_id AS operatorId, status AS status, remark AS remark,
               created_at AS createdAt, updated_at AS updatedAt
        FROM sales_order
        WHERE (#{keyword} IS NULL OR order_no LIKE CONCAT('%', #{keyword}, '%'))
          AND (#{subjectId} IS NULL OR customer_id = #{subjectId})
          AND (#{startDate} IS NULL OR DATE(created_at) >= #{startDate})
          AND (#{endDate} IS NULL OR DATE(created_at) <= #{endDate})
        ORDER BY created_at DESC, id DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """)
    List<SalesOrder> findAll(@Param("keyword") String keyword,
                             @Param("subjectId") Long subjectId,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             @Param("offset") int offset,
                             @Param("pageSize") int pageSize);

    @Update("""
        UPDATE sales_order
        SET status = #{status}
        WHERE id = #{id}
        """)
    int updateStatusById(@Param("id") Long id, @Param("status") String status);
}
