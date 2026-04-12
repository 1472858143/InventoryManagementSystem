package com.supermarket.inventory.user.mapper;

import com.supermarket.inventory.user.entity.UserRole;
import com.supermarket.inventory.user.model.UserRoleRelationView;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    @Insert("""
        <script>
        INSERT INTO user_role (
            user_id,
            role_id
        ) VALUES
        <foreach collection="userRoles" item="item" separator=",">
            (#{item.userId}, #{item.roleId})
        </foreach>
        </script>
        """)
    int batchInsert(@Param("userRoles") List<UserRole> userRoles);

    @Select("""
        <script>
        SELECT
            ur.user_id AS userId,
            r.role_code AS roleCode
        FROM user_role ur
        INNER JOIN role r ON ur.role_id = r.id
        WHERE ur.user_id IN
        <foreach collection="userIds" item="userId" open="(" separator="," close=")">
            #{userId}
        </foreach>
        ORDER BY ur.user_id ASC, r.id ASC
        </script>
        """)
    List<UserRoleRelationView> findRoleCodesByUserIds(@Param("userIds") List<Long> userIds);
}
