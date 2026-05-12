package com.supermarket.inventory.product.mapper;

import com.supermarket.inventory.product.entity.Product;
import com.supermarket.inventory.product.model.ProductView;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("""
        SELECT
            id AS id,
            product_code AS productCode,
            product_name AS productName,
            category_id AS categoryId,
            unit AS unit,
            purchase_price AS purchasePrice,
            sale_price AS salePrice,
            status AS status,
            create_time AS createTime
        FROM product
        WHERE product_code = #{productCode}
        """)
    Product findByProductCode(@Param("productCode") String productCode);

    @Insert("""
        INSERT INTO product (
            product_code,
            product_name,
            category_id,
            unit,
            purchase_price,
            sale_price,
            status
        ) VALUES (
            #{productCode},
            #{productName},
            #{categoryId},
            #{unit},
            #{purchasePrice},
            #{salePrice},
            #{status}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Select("""
        SELECT
            id AS id,
            product_code AS productCode,
            product_name AS productName,
            category_id AS categoryId,
            unit AS unit,
            purchase_price AS purchasePrice,
            sale_price AS salePrice,
            status AS status,
            create_time AS createTime
        FROM product
        ORDER BY id ASC
        """)
    List<Product> findAll();

    @Select("""
        SELECT
            id AS id,
            product_code AS productCode,
            product_name AS productName,
            category_id AS categoryId,
            unit AS unit,
            purchase_price AS purchasePrice,
            sale_price AS salePrice,
            status AS status,
            create_time AS createTime
        FROM product
        WHERE id = #{productId}
        """)
    Product findById(@Param("productId") Long productId);

    @Select("""
        SELECT p.id AS id, p.product_code AS productCode, p.product_name AS productName,
               p.category_id AS categoryId, c.category_name AS categoryName,
               p.unit AS unit, p.purchase_price AS purchasePrice,
               p.sale_price AS salePrice, COALESCE(s.sales_count, 0) AS salesCount,
               p.status AS status, p.create_time AS createTime
        FROM product p INNER JOIN category c ON c.id = p.category_id
        LEFT JOIN (
            SELECT product_id, COALESCE(SUM(ABS(change_quantity)), 0) AS sales_count
            FROM stock_log
            WHERE change_type = 'OUTBOUND'
            GROUP BY product_id
        ) s ON s.product_id = p.id
        ORDER BY p.id ASC
        """)
    List<ProductView> findAllWithCategory();

    @Select("""
        SELECT p.id AS id, p.product_code AS productCode, p.product_name AS productName,
               p.category_id AS categoryId, c.category_name AS categoryName,
               p.unit AS unit, p.purchase_price AS purchasePrice,
               p.sale_price AS salePrice, p.status AS status, p.create_time AS createTime
        FROM product p INNER JOIN category c ON c.id = p.category_id
        WHERE p.id = #{productId}
        """)
    ProductView findByIdWithCategory(@Param("productId") Long productId);

    @Update("""
        UPDATE product
        SET status = #{status}
        WHERE id = #{productId}
        """)
    int updateStatusById(@Param("productId") Long productId, @Param("status") Integer status);
}
