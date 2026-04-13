package com.supermarket.inventory.product.mapper;

import com.supermarket.inventory.product.entity.Product;
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
            category AS category,
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
            category,
            purchase_price,
            sale_price,
            status
        ) VALUES (
            #{productCode},
            #{productName},
            #{category},
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
            category AS category,
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
            category AS category,
            purchase_price AS purchasePrice,
            sale_price AS salePrice,
            status AS status,
            create_time AS createTime
        FROM product
        WHERE id = #{productId}
        """)
    Product findById(@Param("productId") Long productId);

    @Update("""
        UPDATE product
        SET status = #{status}
        WHERE id = #{productId}
        """)
    int updateStatusById(@Param("productId") Long productId, @Param("status") Integer status);
}
