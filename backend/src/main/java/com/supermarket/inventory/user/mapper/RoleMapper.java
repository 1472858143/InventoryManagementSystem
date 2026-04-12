package com.supermarket.inventory.user.mapper;

import com.supermarket.inventory.user.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper {

    @Select("""
        <script>
        SELECT
            id AS id,
            role_name AS roleName,
            role_code AS roleCode,
            remark AS remark,
            create_time AS createTime
        FROM role
        WHERE id IN
        <foreach collection="roleIds" item="roleId" open="(" separator="," close=")">
            #{roleId}
        </foreach>
        ORDER BY id ASC
        </script>
        """)
    List<Role> findByIds(@Param("roleIds") List<Long> roleIds);
}
