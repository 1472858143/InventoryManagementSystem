package com.supermarket.inventory.auth.mapper;

import com.supermarket.inventory.auth.model.AuthUserAuthInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuthMapper {

    @Select("""
        SELECT
            u.id AS userId,
            u.username AS username,
            u.password AS passwordHash,
            u.status AS status,
            r.role_code AS roleCode
        FROM user u
        LEFT JOIN user_role ur ON u.id = ur.user_id
        LEFT JOIN role r ON ur.role_id = r.id
        WHERE u.username = #{username}
        """)
    List<AuthUserAuthInfo> findAuthInfoByUsername(@Param("username") String username);
}
