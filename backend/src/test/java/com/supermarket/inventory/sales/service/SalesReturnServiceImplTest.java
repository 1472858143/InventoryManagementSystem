package com.supermarket.inventory.sales.service;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.customer.entity.Customer;
import com.supermarket.inventory.customer.mapper.CustomerMapper;
import com.supermarket.inventory.product.entity.Product;
import com.supermarket.inventory.product.mapper.ProductMapper;
import com.supermarket.inventory.sales.dto.SalesReturnCreateRequest;
import com.supermarket.inventory.sales.dto.SalesReturnItemRequest;
import com.supermarket.inventory.sales.entity.SalesReturnOrder;
import com.supermarket.inventory.sales.entity.SalesReturnOrderItem;
import com.supermarket.inventory.sales.mapper.SalesReturnOrderItemMapper;
import com.supermarket.inventory.sales.mapper.SalesReturnOrderMapper;
import com.supermarket.inventory.sales.service.impl.SalesReturnServiceImpl;
import com.supermarket.inventory.sales.vo.SalesReturnDetailResponse;
import com.supermarket.inventory.sales.vo.SalesReturnSummaryResponse;
import com.supermarket.inventory.stock.change.SourceType;
import com.supermarket.inventory.stock.change.StockChangeCommand;
import com.supermarket.inventory.stock.change.StockChangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesReturnServiceImplTest {

    @Mock
    private SalesReturnOrderMapper returnOrderMapper;

    @Mock
    private SalesReturnOrderItemMapper returnOrderItemMapper;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private StockChangeService stockChangeService;

    private SalesReturnServiceImpl salesReturnService;

    @BeforeEach
    void setUp() {
        salesReturnService = new SalesReturnServiceImpl(
            returnOrderMapper,
            returnOrderItemMapper,
            customerMapper,
            productMapper,
            stockChangeService
        );
    }

    @Test
    void createShouldPersistReturnAndIncreaseStock() {
        when(customerMapper.findById(1L)).thenReturn(customer(1L, 1));
        when(productMapper.findById(10L)).thenReturn(product(10L, 1));
        doAnswer(invocation -> {
            SalesReturnOrder order = invocation.getArgument(0);
            order.setId(300L);
            return 1;
        }).when(returnOrderMapper).insert(any(SalesReturnOrder.class));

        SalesReturnDetailResponse response = salesReturnService.create(new SalesReturnCreateRequest(
            1L,
            200L,
            7L,
            "客户退货",
            List.of(new SalesReturnItemRequest(10L, 2, decimal("6.50"), "A"))
        ));

        assertNotNull(response.returnNo());
        assertTrue(response.returnNo().matches("SRO\\d{18}"));
        assertEquals(300L, response.id());
        assertEquals(1L, response.customerId());
        assertEquals(200L, response.sourceOrderId());
        assertEquals(2, response.totalQuantity());
        assertEquals(0, decimal("13.00").compareTo(response.totalAmount()));

        ArgumentCaptor<StockChangeCommand> commandCaptor = ArgumentCaptor.forClass(StockChangeCommand.class);
        verify(stockChangeService).apply(commandCaptor.capture());
        assertEquals(SourceType.SALE_RETURN_IN, commandCaptor.getValue().sourceType());
        assertEquals(300L, commandCaptor.getValue().sourceId());
        assertEquals(2, commandCaptor.getValue().delta());
    }

    @Test
    void createShouldRejectDuplicateProducts() {
        when(customerMapper.findById(1L)).thenReturn(customer(1L, 1));
        when(productMapper.findById(10L)).thenReturn(product(10L, 1));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            salesReturnService.create(new SalesReturnCreateRequest(
                1L,
                null,
                7L,
                "退货",
                List.of(
                    new SalesReturnItemRequest(10L, 1, decimal("6.50"), null),
                    new SalesReturnItemRequest(10L, 2, decimal("6.50"), null)
                )
            ))
        );

        assertEquals(400, ex.getCode());
        assertEquals("同一张客户退货单不能重复选择商品", ex.getMessage());
        verify(returnOrderMapper, never()).insert(any(SalesReturnOrder.class));
    }

    @Test
    void listShouldReturnPagedSalesReturnsWithFilters() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 31);
        SalesReturnOrder order = returnOrder(300L, "SRO2026051400010001", "NORMAL");
        when(returnOrderMapper.count("SRO202605", 1L, startDate, endDate)).thenReturn(11L);
        when(returnOrderMapper.findAll("SRO202605", 1L, startDate, endDate, 10, 10))
            .thenReturn(List.of(order));

        PageResponse<SalesReturnSummaryResponse> response = salesReturnService.list(new OrderQueryRequest(
            "SRO202605",
            1L,
            startDate,
            endDate,
            2,
            10
        ));

        assertEquals(11L, response.total());
        assertEquals(2, response.page());
        assertEquals(10, response.pageSize());
        assertEquals(1, response.items().size());
        assertEquals("SRO2026051400010001", response.items().get(0).returnNo());
    }

    @Test
    void detailShouldReturnSalesReturnWithItems() {
        when(returnOrderMapper.findById(300L)).thenReturn(returnOrder(300L, "SRO2026051400010001", "NORMAL"));
        when(returnOrderItemMapper.findByReturnId(300L)).thenReturn(List.of(returnItem(10L, 2)));

        SalesReturnDetailResponse response = salesReturnService.detail(300L);

        assertEquals(300L, response.id());
        assertEquals("SRO2026051400010001", response.returnNo());
        assertEquals(1, response.items().size());
        assertEquals(10L, response.items().get(0).productId());
    }

    @Test
    void cancelShouldMarkOrderAndWriteReverseStockChanges() {
        SalesReturnOrder order = returnOrder(300L, "SRO2026051400010001", "NORMAL");
        when(returnOrderMapper.findById(300L)).thenReturn(order);
        when(returnOrderItemMapper.findByReturnId(300L)).thenReturn(List.of(returnItem(10L, 2)));

        salesReturnService.cancel(300L, 7L);

        verify(returnOrderMapper).updateStatusById(300L, "CANCELLED");
        ArgumentCaptor<StockChangeCommand> commandCaptor = ArgumentCaptor.forClass(StockChangeCommand.class);
        verify(stockChangeService).apply(commandCaptor.capture());
        assertEquals(SourceType.SALE_RETURN_IN, commandCaptor.getValue().sourceType());
        assertEquals(300L, commandCaptor.getValue().sourceId());
        assertEquals(-2, commandCaptor.getValue().delta());
        assertEquals("作废冲销：SRO2026051400010001", commandCaptor.getValue().reason());
    }

    @Test
    void cancelShouldRejectAlreadyCancelledOrder() {
        when(returnOrderMapper.findById(300L)).thenReturn(returnOrder(300L, "SRO2026051400010001", "CANCELLED"));

        BusinessException ex = assertThrows(BusinessException.class, () -> salesReturnService.cancel(300L, 7L));

        assertEquals(400, ex.getCode());
        assertEquals("单据已作废", ex.getMessage());
        verify(stockChangeService, never()).apply(any(StockChangeCommand.class));
    }

    @Test
    void cancelShouldPropagateInsufficientStockFromReverseChange() {
        SalesReturnOrder order = returnOrder(300L, "SRO2026051400010001", "NORMAL");
        when(returnOrderMapper.findById(300L)).thenReturn(order);
        when(returnOrderItemMapper.findByReturnId(300L)).thenReturn(List.of(returnItem(10L, 2)));
        when(stockChangeService.apply(any(StockChangeCommand.class)))
            .thenThrow(new BusinessException(400, "库存不足，当前库存：1"));

        BusinessException ex = assertThrows(BusinessException.class, () -> salesReturnService.cancel(300L, 7L));

        assertEquals(400, ex.getCode());
        assertEquals("库存不足，当前库存：1", ex.getMessage());
    }

    private Customer customer(Long id, Integer status) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName("客户" + id);
        customer.setStatus(status);
        return customer;
    }

    private Product product(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setProductName("商品" + id);
        product.setStatus(status);
        return product;
    }

    private SalesReturnOrder returnOrder(Long id, String returnNo, String status) {
        SalesReturnOrder order = new SalesReturnOrder();
        order.setId(id);
        order.setReturnNo(returnNo);
        order.setCustomerId(1L);
        order.setSourceOrderId(200L);
        order.setOperatorId(7L);
        order.setTotalQuantity(2);
        order.setTotalAmount(decimal("13.00"));
        order.setStatus(status);
        order.setReason("原因");
        order.setCreatedAt(LocalDateTime.of(2026, 5, 14, 10, 0));
        return order;
    }

    private SalesReturnOrderItem returnItem(Long productId, Integer quantity) {
        SalesReturnOrderItem item = new SalesReturnOrderItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(decimal("6.50"));
        item.setSubtotal(decimal("13.00"));
        return item;
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
