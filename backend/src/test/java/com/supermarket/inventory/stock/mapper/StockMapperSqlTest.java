package com.supermarket.inventory.stock.mapper;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StockMapperSqlTest {

    @Test
    void findEntityByProductIdForUpdateShouldLockStockRow() throws Exception {
        Method method = StockMapper.class.getMethod("findEntityByProductIdForUpdate", Long.class);
        String xml = Files.readString(Path.of("src/main/resources/mapper/StockMapper.xml"))
            .replaceAll("\\s+", " ")
            .toLowerCase(Locale.ROOT);

        assertTrue(method.getName().equals("findEntityByProductIdForUpdate"));
        assertTrue(
            xml.contains("id=\"findentitybyproductidforupdate\"") && xml.contains("for update"),
            "库存扣减必须通过 SELECT ... FOR UPDATE 锁定 stock 行"
        );
    }
}
