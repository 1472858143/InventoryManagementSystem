package com.supermarket.inventory.sales.service;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.customer.entity.Customer;
import com.supermarket.inventory.customer.mapper.CustomerMapper;
import com.supermarket.inventory.product.entity.Product;
import com.supermarket.inventory.product.mapper.ProductMapper;
import com.supermarket.inventory.sales.dto.SalesOrderCreateRequest;
import com.supermarket.inventory.sales.dto.SalesOrderItemRequest;
import com.supermarket.inventory.sales.entity.SalesOrder;
import com.supermarket.inventory.sales.entity.SalesOrderItem;
import com.supermarket.inventory.sales.mapper.SalesOrderItemMapper;
import com.supermarket.inventory.sales.mapper.SalesOrderMapper;
import com.supermarket.inventory.sales.service.impl.SalesServiceImpl;
import com.supermarket.inventory.sales.vo.SalesOrderDetailResponse;
import com.supermarket.inventory.sales.vo.SalesOrderSummaryResponse;
import com.supermarket.inventory.stock.change.SourceType;
import com.supermarket.inventory.stock.change.StockChangeCommand;
import com.supermarket.inventory.stock.change.StockChangeResult;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesServiceImplTest {

    @Mock
    private SalesOrderMapper salesOrderMapper;

    @Mock
    private SalesOrderItemMapper salesOrderItemMapper;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private StockChangeService stockChangeService;

    private SalesServiceImpl salesService;

    @BeforeEach
    void setUp() {
        salesService = new SalesServiceImpl(
            salesOrderMapper,
            salesOrderItemMapper,
            customerMapper,
            productMapper,
            stockChangeService
        );
    }

    @Test
    void createShouldPersistMultiItemSaleAndDecreaseStock() {
        when(customerMapper.findById(1L)).thenReturn(customer(1L, 1));
        when(productMapper.findById(10L)).thenReturn(product(10L, 1));
        when(productMapper.findById(11L)).thenReturn(product(11L, 1));
        doAnswer(invocation -> {
            SalesOrder order = invocation.getArgument(0);
            order.setId(200L);
            return 1;
        }).when(salesOrderMapper).insert(any(SalesOrder.class));
        when(stockChangeService.apply(any(StockChangeCommand.class)))
            .thenReturn(new StockChangeResult(10L, 10, 8))
            .thenReturn(new StockChangeResult(11L, 10, 7));

        SalesOrderDetailResponse response = salesService.create(new SalesOrderCreateRequest(
            1L,
            7L,
            "首批销售",
            List.of(
                new SalesOrderItemRequest(10L, 2, decimal("6.50"), "A"),
                new SalesOrderItemRequest(11L, 3, decimal("4.00"), "B")
            )
        ));

        assertNotNull(response.orderNo());
        assertTrue(response.orderNo().matches("SO\\d{18}"));
        assertEquals(200L, response.id());
        assertEquals(1L, response.customerId());
        assertEquals(7L, response.operatorId());
        assertEquals(5, response.totalQuantity());
        assertEquals(0, decimal("25.00").compareTo(response.totalAmount()));
        assertEquals(2, response.items().size());

        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(salesOrderMapper).insert(orderCaptor.capture());
        assertEquals("NORMAL", orderCaptor.getValue().getStatus());
        assertEquals(5, orderCaptor.getValue().getTotalQuantity());
        assertEquals(0, decimal("25.00").compareTo(orderCaptor.getValue().getTotalAmount()));

        ArgumentCaptor<SalesOrderItem> itemCaptor = ArgumentCaptor.forClass(SalesOrderItem.class);
        verify(salesOrderItemMapper, times(2)).insert(itemCaptor.capture());
        assertEquals(0, decimal("13.00").compareTo(itemCaptor.getAllValues().get(0).getSubtotal()));
        assertEquals(0, decimal("12.00").compareTo(itemCaptor.getAllValues().get(1).getSubtotal()));

        ArgumentCaptor<StockChangeCommand> commandCaptor = ArgumentCaptor.forClass(StockChangeCommand.class);
        verify(stockChangeService, times(2)).apply(commandCaptor.capture());
        assertEquals(SourceType.SALE_OUT, commandCaptor.getAllValues().get(0).sourceType());
        assertEquals(200L, commandCaptor.getAllValues().get(0).sourceId());
        assertEquals(-2, commandCaptor.getAllValues().get(0).delta());
        assertEquals(-3, commandCaptor.getAllValues().get(1).delta());
    }

    @Test
    void createShouldRejectEmptyItems() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
            salesService.create(new SalesOrderCreateRequest(1L, 7L, "备注", List.of()))
        );

        assertEquals(400, ex.getCode());
        assertEquals("销售明细不能为空", ex.getMessage());
        verify(salesOrderMapper, never()).insert(any(SalesOrder.class));
    }

    @Test
    void createShouldRejectDuplicateProducts() {
        when(customerMapper.findById(1L)).thenReturn(customer(1L, 1));
        when(productMapper.findById(10L)).thenReturn(product(10L, 1));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            salesService.create(new SalesOrderCreateRequest(
                1L,
                7L,
                "备注",
                List.of(
                    new SalesOrderItemRequest(10L, 1, decimal("6.50"), null),
                    new SalesOrderItemRequest(10L, 2, decimal("6.50"), null)
                )
            ))
        );

        assertEquals(400, ex.getCode());
        assertEquals("同一张销售单不能重复选择商品", ex.getMessage());
        verify(salesOrderMapper, never()).insert(any(SalesOrder.class));
    }

    @Test
    void createShouldRejectDisabledCustomer() {
        when(customerMapper.findById(1L)).thenReturn(customer(1L, 0));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            salesService.create(new SalesOrderCreateRequest(
                1L,
                7L,
                "备注",
                List.of(new SalesOrderItemRequest(10L, 1, decimal("6.50"), null))
            ))
        );

        assertEquals(400, ex.getCode());
        assertEquals("客户已停用", ex.getMessage());
        verify(salesOrderMapper, never()).insert(any(SalesOrder.class));
    }

    @Test
    void createShouldRejectDisabledProduct() {
        when(customerMapper.findById(1L)).thenReturn(customer(1L, 1));
        when(productMapper.findById(10L)).thenReturn(product(10L, 0));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            salesService.create(new SalesOrderCreateRequest(
                1L,
                7L,
                "备注",
                List.of(new SalesOrderItemRequest(10L, 1, decimal("6.50"), null))
            ))
        );

        assertEquals(400, ex.getCode());
        assertEquals("商品已停用", ex.getMessage());
        verify(salesOrderMapper, never()).insert(any(SalesOrder.class));
    }

    @Test
    void createShouldPropagateInsufficientStockFromStockChange() {
        when(customerMapper.findById(1L)).thenReturn(customer(1L, 1));
        when(productMapper.findById(10L)).thenReturn(product(10L, 1));
        doAnswer(invocation -> {
            SalesOrder order = invocation.getArgument(0);
            order.setId(200L);
            return 1;
        }).when(salesOrderMapper).insert(any(SalesOrder.class));
        when(stockChangeService.apply(any(StockChangeCommand.class)))
            .thenThrow(new BusinessException(400, "库存不足，当前库存：1"));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            salesService.create(new SalesOrderCreateRequest(
                1L,
                7L,
                "备注",
                List.of(new SalesOrderItemRequest(10L, 2, decimal("6.50"), null))
            ))
        );

        assertEquals(400, ex.getCode());
        assertEquals("库存不足，当前库存：1", ex.getMessage());
        verify(stockChangeService).apply(any(StockChangeCommand.class));
    }

    @Test
    void listShouldReturnPagedSalesOrdersWithFilters() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 31);
        SalesOrder order = salesOrder(200L, "SO2026051400010001", "NORMAL");
        when(salesOrderMapper.count("SO202605", 1L, startDate, endDate)).thenReturn(21L);
        when(salesOrderMapper.findAll("SO202605", 1L, startDate, endDate, 20, 20))
            .thenReturn(List.of(order));

        PageResponse<SalesOrderSummaryResponse> response = salesService.list(new OrderQueryRequest(
            "SO202605",
            1L,
            startDate,
            endDate,
            2,
            20
        ));

        assertEquals(21L, response.total());
        assertEquals(2, response.page());
        assertEquals(20, response.pageSize());
        assertEquals(1, response.items().size());
        assertEquals("SO2026051400010001", response.items().get(0).orderNo());
    }

    @Test
    void detailShouldReturnSalesOrderWithItems() {
        when(salesOrderMapper.findById(200L)).thenReturn(salesOrder(200L, "SO2026051400010001", "NORMAL"));
        when(salesOrderItemMapper.findByOrderId(200L)).thenReturn(List.of(salesItem(10L, 2)));

        SalesOrderDetailResponse response = salesService.detail(200L);

        assertEquals(200L, response.id());
        assertEquals("SO2026051400010001", response.orderNo());
        assertEquals(1, response.items().size());
        assertEquals(10L, response.items().get(0).productId());
    }

    @Test
    void cancelShouldMarkOrderAndWriteReverseStockChanges() {
        SalesOrder order = salesOrder(200L, "SO2026051400010001", "NORMAL");
        when(salesOrderMapper.findById(200L)).thenReturn(order);
        when(salesOrderItemMapper.findByOrderId(200L)).thenReturn(List.of(salesItem(10L, 2)));

        salesService.cancel(200L, 7L);

        verify(salesOrderMapper).updateStatusById(200L, "CANCELLED");
        ArgumentCaptor<StockChangeCommand> commandCaptor = ArgumentCaptor.forClass(StockChangeCommand.class);
        verify(stockChangeService).apply(commandCaptor.capture());
        assertEquals(SourceType.SALE_OUT, commandCaptor.getValue().sourceType());
        assertEquals(200L, commandCaptor.getValue().sourceId());
        assertEquals(2, commandCaptor.getValue().delta());
        assertEquals("作废冲销：SO2026051400010001", commandCaptor.getValue().reason());
    }

    @Test
    void cancelShouldRejectAlreadyCancelledSalesOrder() {
        when(salesOrderMapper.findById(200L)).thenReturn(salesOrder(200L, "SO2026051400010001", "CANCELLED"));

        BusinessException ex = assertThrows(BusinessException.class, () -> salesService.cancel(200L, 7L));

        assertEquals(400, ex.getCode());
        assertEquals("单据已作废", ex.getMessage());
        verify(stockChangeService, never()).apply(any(StockChangeCommand.class));
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

    private SalesOrder salesOrder(Long id, String orderNo, String status) {
        SalesOrder order = new SalesOrder();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setCustomerId(1L);
        order.setOperatorId(7L);
        order.setTotalQuantity(2);
        order.setTotalAmount(decimal("13.00"));
        order.setStatus(status);
        order.setRemark("备注");
        order.setCreatedAt(LocalDateTime.of(2026, 5, 14, 10, 0));
        return order;
    }

    private SalesOrderItem salesItem(Long productId, Integer quantity) {
        SalesOrderItem item = new SalesOrderItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(decimal("6.50"));
        item.setSubtotal(decimal("13.00"));
        item.setRemark("A");
        return item;
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
