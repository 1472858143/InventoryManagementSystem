package com.supermarket.inventory.user.mapper;

import com.supermarket.inventory.user.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("""
        SELECT
            id AS id,
            username AS username,
            password AS password,
            real_name AS realName,
            status AS status,
            create_time AS createTime
        FROM user
        WHERE username = #{username}
        """)
    User findByUsername(@Param("username") String username);

    @Insert("""
        INSERT INTO user (
            username,
            password,
            real_name,
            status
        ) VALUES (
            #{username},
            #{password},
            #{realName},
            #{status}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("""
        SELECT
            id AS id,
            username AS username,
            real_name AS realName,
            status AS status,
            create_time AS createTime
        FROM user
        ORDER BY id ASC
        """)
    List<User> findAll();

    @Select("""
        SELECT
            id AS id,
            username AS username,
            password AS password,
            real_name AS realName,
            status AS status,
            create_time AS createTime
        FROM user
        WHERE id = #{userId}
        """)
    User findById(@Param("userId") Long userId);

    @Update("""
        UPDATE user
        SET status = #{status}
        WHERE id = #{userId}
        """)
    int updateStatusById(@Param("userId") Long userId, @Param("status") Integer status);
}
