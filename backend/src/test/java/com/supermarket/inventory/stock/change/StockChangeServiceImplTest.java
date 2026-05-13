package com.supermarket.inventory.stock.change;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.stock.change.impl.StockChangeServiceImpl;
import com.supermarket.inventory.stock.entity.Stock;
import com.supermarket.inventory.stock.entity.StockLog;
import com.supermarket.inventory.stock.mapper.StockLogMapper;
import com.supermarket.inventory.stock.mapper.StockMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockChangeServiceImplTest {

    @Mock
    private StockMapper stockMapper;

    @Mock
    private StockLogMapper stockLogMapper;

    private StockChangeServiceImpl stockChangeService;

    @BeforeEach
    void setUp() {
        stockChangeService = new StockChangeServiceImpl(stockMapper, stockLogMapper);
    }

    @Test
    void applyShouldIncreaseStockAndWriteSourceLog() {
        when(stockMapper.findEntityByProductIdForUpdate(1L)).thenReturn(stock(1L, 10));

        StockChangeResult result = stockChangeService.apply(new StockChangeCommand(
            1L,
            5,
            SourceType.PURCHASE_IN,
            100L,
            7L,
            "进货入库"
        ));

        assertEquals(10, result.beforeQuantity());
        assertEquals(15, result.afterQuantity());
        verify(stockMapper).updateQuantityByProductId(1L, 15);

        ArgumentCaptor<StockLog> captor = ArgumentCaptor.forClass(StockLog.class);
        verify(stockLogMapper).insert(captor.capture());
        StockLog log = captor.getValue();
        assertEquals(1L, log.getProductId());
        assertEquals("INBOUND", log.getChangeType());
        assertEquals("WAREHOUSE", log.getStockType());
        assertEquals(5, log.getChangeQuantity());
        assertEquals(10, log.getBeforeQuantity());
        assertEquals(15, log.getAfterQuantity());
        assertEquals("PURCHASE_IN", log.getSourceType());
        assertEquals(100L, log.getSourceId());
        assertEquals(7L, log.getOperatorId());
        assertEquals("进货入库", log.getReason());
    }

    @Test
    void applyShouldDecreaseStockAndWriteOutboundLog() {
        when(stockMapper.findEntityByProductIdForUpdate(1L)).thenReturn(stock(1L, 10));

        StockChangeResult result = stockChangeService.apply(new StockChangeCommand(
            1L,
            -3,
            SourceType.SALE_OUT,
            200L,
            8L,
            "销售出库"
        ));

        assertEquals(10, result.beforeQuantity());
        assertEquals(7, result.afterQuantity());
        verify(stockMapper).updateQuantityByProductId(1L, 7);

        ArgumentCaptor<StockLog> captor = ArgumentCaptor.forClass(StockLog.class);
        verify(stockLogMapper).insert(captor.capture());
        StockLog log = captor.getValue();
        assertEquals("OUTBOUND", log.getChangeType());
        assertEquals(-3, log.getChangeQuantity());
        assertEquals("SALE_OUT", log.getSourceType());
        assertEquals(200L, log.getSourceId());
    }

    @Test
    void applyShouldRejectInsufficientStock() {
        when(stockMapper.findEntityByProductIdForUpdate(1L)).thenReturn(stock(1L, 2));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            stockChangeService.apply(new StockChangeCommand(
                1L,
                -3,
                SourceType.SALE_OUT,
                200L,
                8L,
                "销售出库"
            ))
        );

        assertEquals(400, ex.getCode());
        assertEquals("库存不足，当前库存：2", ex.getMessage());
        verify(stockMapper, never()).updateQuantityByProductId(1L, -1);
        verify(stockLogMapper, never()).insert(org.mockito.ArgumentMatchers.any(StockLog.class));
    }

    @Test
    void applyShouldRejectMissingStock() {
        when(stockMapper.findEntityByProductIdForUpdate(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
            stockChangeService.apply(new StockChangeCommand(
                1L,
                5,
                SourceType.PURCHASE_IN,
                100L,
                7L,
                "进货入库"
            ))
        );

        assertEquals(404, ex.getCode());
        assertEquals("库存记录不存在", ex.getMessage());
        verify(stockMapper, never()).updateQuantityByProductId(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void applyShouldRejectZeroDelta() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
            stockChangeService.apply(new StockChangeCommand(
                1L,
                0,
                SourceType.PURCHASE_IN,
                100L,
                7L,
                "进货入库"
            ))
        );

        assertEquals(400, ex.getCode());
        assertEquals("库存变更数量不能为0", ex.getMessage());
        verify(stockMapper, never()).findEntityByProductIdForUpdate(org.mockito.ArgumentMatchers.anyLong());
    }

    private Stock stock(Long productId, Integer quantity) {
        Stock stock = new Stock();
        stock.setId(10L);
        stock.setProductId(productId);
        stock.setQuantity(quantity);
        return stock;
    }
}
