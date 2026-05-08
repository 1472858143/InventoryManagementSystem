package com.supermarket.inventory.category.mapper;

import com.supermarket.inventory.category.entity.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT id, category_name AS categoryName, status, create_time AS createTime FROM category ORDER BY id ASC")
    List<Category> findAll();

    @Select("SELECT id, category_name AS categoryName, status, create_time AS createTime FROM category WHERE status = 1 ORDER BY id ASC")
    List<Category> findAllEnabled();

    @Select("SELECT id, category_name AS categoryName, status, create_time AS createTime FROM category WHERE id = #{id}")
    Category findById(@Param("id") Long id);

    @Insert("INSERT INTO category (category_name) VALUES (#{categoryName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE category SET status = #{status} WHERE id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);
}
