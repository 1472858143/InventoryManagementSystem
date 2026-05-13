package com.supermarket.inventory.purchase.service;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.product.entity.Product;
import com.supermarket.inventory.product.mapper.ProductMapper;
import com.supermarket.inventory.purchase.dto.PurchaseReturnCreateRequest;
import com.supermarket.inventory.purchase.dto.PurchaseReturnItemRequest;
import com.supermarket.inventory.purchase.entity.PurchaseReturnOrder;
import com.supermarket.inventory.purchase.entity.PurchaseReturnOrderItem;
import com.supermarket.inventory.purchase.mapper.PurchaseReturnOrderItemMapper;
import com.supermarket.inventory.purchase.mapper.PurchaseReturnOrderMapper;
import com.supermarket.inventory.purchase.service.impl.PurchaseReturnServiceImpl;
import com.supermarket.inventory.purchase.vo.PurchaseReturnDetailResponse;
import com.supermarket.inventory.purchase.vo.PurchaseReturnSummaryResponse;
import com.supermarket.inventory.stock.change.SourceType;
import com.supermarket.inventory.stock.change.StockChangeCommand;
import com.supermarket.inventory.stock.change.StockChangeService;
import com.supermarket.inventory.supplier.entity.Supplier;
import com.supermarket.inventory.supplier.mapper.SupplierMapper;
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
class PurchaseReturnServiceImplTest {

    @Mock
    private PurchaseReturnOrderMapper returnOrderMapper;

    @Mock
    private PurchaseReturnOrderItemMapper returnOrderItemMapper;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private StockChangeService stockChangeService;

    private PurchaseReturnServiceImpl purchaseReturnService;

    @BeforeEach
    void setUp() {
        purchaseReturnService = new PurchaseReturnServiceImpl(
            returnOrderMapper,
            returnOrderItemMapper,
            supplierMapper,
            productMapper,
            stockChangeService
        );
    }

    @Test
    void createShouldPersistReturnAndDecreaseStock() {
        when(supplierMapper.findById(1L)).thenReturn(supplier(1L, 1));
        when(productMapper.findById(10L)).thenReturn(product(10L, 1));
        doAnswer(invocation -> {
            PurchaseReturnOrder order = invocation.getArgument(0);
            order.setId(200L);
            return 1;
        }).when(returnOrderMapper).insert(any(PurchaseReturnOrder.class));

        PurchaseReturnDetailResponse response = purchaseReturnService.create(new PurchaseReturnCreateRequest(
            1L,
            null,
            7L,
            "破损退货",
            List.of(new PurchaseReturnItemRequest(10L, 2, decimal("3.50"), "A"))
        ));

        assertNotNull(response.returnNo());
        assertTrue(response.returnNo().matches("PRO\\d{18}"));
        assertEquals(200L, response.id());
        assertEquals(2, response.totalQuantity());
        assertEquals(0, decimal("7.00").compareTo(response.totalAmount()));

        ArgumentCaptor<StockChangeCommand> commandCaptor = ArgumentCaptor.forClass(StockChangeCommand.class);
        verify(stockChangeService).apply(commandCaptor.capture());
        assertEquals(SourceType.PURCHASE_RETURN_OUT, commandCaptor.getValue().sourceType());
        assertEquals(200L, commandCaptor.getValue().sourceId());
        assertEquals(-2, commandCaptor.getValue().delta());
    }

    @Test
    void createShouldRejectDuplicateProducts() {
        when(supplierMapper.findById(1L)).thenReturn(supplier(1L, 1));
        when(productMapper.findById(10L)).thenReturn(product(10L, 1));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            purchaseReturnService.create(new PurchaseReturnCreateRequest(
                1L,
                null,
                7L,
                "退货",
                List.of(
                    new PurchaseReturnItemRequest(10L, 1, decimal("3.50"), null),
                    new PurchaseReturnItemRequest(10L, 2, decimal("3.50"), null)
                )
            ))
        );

        assertEquals(400, ex.getCode());
        assertEquals("同一张采购退货单不能重复选择商品", ex.getMessage());
        verify(returnOrderMapper, never()).insert(any(PurchaseReturnOrder.class));
    }

    @Test
    void listShouldReturnPagedPurchaseReturnsWithFilters() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 31);
        PurchaseReturnOrder order = returnOrder(200L, "PRO2026051400010001", "NORMAL");
        when(returnOrderMapper.count("PRO202605", 1L, startDate, endDate)).thenReturn(11L);
        when(returnOrderMapper.findAll("PRO202605", 1L, startDate, endDate, 10, 10))
            .thenReturn(List.of(order));

        PageResponse<PurchaseReturnSummaryResponse> response = purchaseReturnService.list(new OrderQueryRequest(
            "PRO202605",
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
        assertEquals("PRO2026051400010001", response.items().get(0).returnNo());
    }

    @Test
    void detailShouldReturnPurchaseReturnWithItems() {
        when(returnOrderMapper.findById(200L)).thenReturn(returnOrder(200L, "PRO2026051400010001", "NORMAL"));
        when(returnOrderItemMapper.findByReturnId(200L)).thenReturn(List.of(returnItem(10L, 2)));

        PurchaseReturnDetailResponse response = purchaseReturnService.detail(200L);

        assertEquals(200L, response.id());
        assertEquals("PRO2026051400010001", response.returnNo());
        assertEquals(1, response.items().size());
        assertEquals(10L, response.items().get(0).productId());
    }

    @Test
    void cancelShouldMarkOrderAndWriteReverseStockChanges() {
        PurchaseReturnOrder order = returnOrder(200L, "PRO2026051400010001", "NORMAL");
        when(returnOrderMapper.findById(200L)).thenReturn(order);
        when(returnOrderItemMapper.findByReturnId(200L)).thenReturn(List.of(returnItem(10L, 2)));

        purchaseReturnService.cancel(200L, 7L);

        verify(returnOrderMapper).updateStatusById(200L, "CANCELLED");
        ArgumentCaptor<StockChangeCommand> commandCaptor = ArgumentCaptor.forClass(StockChangeCommand.class);
        verify(stockChangeService).apply(commandCaptor.capture());
        assertEquals(SourceType.PURCHASE_RETURN_OUT, commandCaptor.getValue().sourceType());
        assertEquals(200L, commandCaptor.getValue().sourceId());
        assertEquals(2, commandCaptor.getValue().delta());
        assertEquals("作废冲销：PRO2026051400010001", commandCaptor.getValue().reason());
    }

    @Test
    void cancelShouldRejectAlreadyCancelledOrder() {
        when(returnOrderMapper.findById(200L)).thenReturn(returnOrder(200L, "PRO2026051400010001", "CANCELLED"));

        BusinessException ex = assertThrows(BusinessException.class, () -> purchaseReturnService.cancel(200L, 7L));

        assertEquals(400, ex.getCode());
        assertEquals("单据已作废", ex.getMessage());
        verify(stockChangeService, never()).apply(any(StockChangeCommand.class));
    }

    private Supplier supplier(Long id, Integer status) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setStatus(status);
        return supplier;
    }

    private Product product(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setProductName("商品" + id);
        product.setStatus(status);
        return product;
    }

    private PurchaseReturnOrder returnOrder(Long id, String returnNo, String status) {
        PurchaseReturnOrder order = new PurchaseReturnOrder();
        order.setId(id);
        order.setReturnNo(returnNo);
        order.setSupplierId(1L);
        order.setSourceOrderId(100L);
        order.setOperatorId(7L);
        order.setTotalQuantity(2);
        order.setTotalAmount(decimal("7.00"));
        order.setStatus(status);
        order.setReason("原因");
        order.setCreatedAt(LocalDateTime.of(2026, 5, 14, 10, 0));
        return order;
    }

    private PurchaseReturnOrderItem returnItem(Long productId, Integer quantity) {
        PurchaseReturnOrderItem item = new PurchaseReturnOrderItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(decimal("3.50"));
        item.setSubtotal(decimal("7.00"));
        return item;
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
