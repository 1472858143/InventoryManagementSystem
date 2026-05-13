package com.supermarket.inventory.supplier.mapper;

import com.supermarket.inventory.supplier.entity.Supplier;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SupplierMapper {

    @Select("""
        SELECT id AS id, code AS code, name AS name,
               contact_person AS contactPerson, phone AS phone,
               address AS address, remark AS remark,
               status AS status, created_at AS createdAt
        FROM supplier
        WHERE code = #{code}
        """)
    Supplier findByCode(@Param("code") String code);

    @Insert("""
        INSERT INTO supplier (code, name, contact_person, phone, address, remark, status)
        VALUES (#{code}, #{name}, #{contactPerson}, #{phone}, #{address}, #{remark}, #{status})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Supplier supplier);

    @Select("""
        SELECT id AS id, code AS code, name AS name,
               contact_person AS contactPerson, phone AS phone,
               address AS address, remark AS remark,
               status AS status, created_at AS createdAt
        FROM supplier
        WHERE (#{keyword} IS NULL OR code LIKE CONCAT('%', #{keyword}, '%')
                                  OR name LIKE CONCAT('%', #{keyword}, '%'))
        ORDER BY id ASC
        """)
    List<Supplier> findAll(@Param("keyword") String keyword);

    @Select("""
        SELECT id AS id, code AS code, name AS name,
               contact_person AS contactPerson, phone AS phone,
               address AS address, remark AS remark,
               status AS status, created_at AS createdAt
        FROM supplier
        WHERE id = #{id}
        """)
    Supplier findById(@Param("id") Long id);

    @Update("""
        UPDATE supplier
        SET name = #{name}, contact_person = #{contactPerson}, phone = #{phone},
            address = #{address}, remark = #{remark}
        WHERE id = #{id}
        """)
    int updateById(@Param("id") Long id, @Param("name") String name,
                   @Param("contactPerson") String contactPerson, @Param("phone") String phone,
                   @Param("address") String address, @Param("remark") String remark);

    @Update("""
        UPDATE supplier
        SET status = #{status}
        WHERE id = #{id}
        """)
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);
}
