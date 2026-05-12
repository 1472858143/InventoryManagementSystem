package com.supermarket.inventory.product.mapper;

import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductMapperTest {

    @Test
    void findAllWithCategoryShouldCountOutboundStockLogAsPositiveSales() throws NoSuchMethodException {
        Method method = ProductMapper.class.getMethod("findAllWithCategory");
        Select select = method.getAnnotation(Select.class);
        String sql = String.join(" ", select.value())
            .replaceAll("\\s+", " ")
            .toLowerCase(Locale.ROOT);

        assertTrue(
            sql.contains("sum(abs(change_quantity))"),
            "商品销量应将 OUTBOUND 库存日志的负向 change_quantity 转换为正向销量"
        );
    }
}
