package com.supermarket.inventory.purchase.service;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.common.request.OrderQueryRequest;
import com.supermarket.inventory.common.response.PageResponse;
import com.supermarket.inventory.product.entity.Product;
import com.supermarket.inventory.product.mapper.ProductMapper;
import com.supermarket.inventory.purchase.dto.PurchaseOrderCreateRequest;
import com.supermarket.inventory.purchase.dto.PurchaseOrderItemRequest;
import com.supermarket.inventory.purchase.entity.PurchaseOrder;
import com.supermarket.inventory.purchase.entity.PurchaseOrderItem;
import com.supermarket.inventory.purchase.mapper.PurchaseOrderItemMapper;
import com.supermarket.inventory.purchase.mapper.PurchaseOrderMapper;
import com.supermarket.inventory.purchase.service.impl.PurchaseServiceImpl;
import com.supermarket.inventory.purchase.vo.PurchaseOrderDetailResponse;
import com.supermarket.inventory.purchase.vo.PurchaseOrderSummaryResponse;
import com.supermarket.inventory.stock.change.SourceType;
import com.supermarket.inventory.stock.change.StockChangeCommand;
import com.supermarket.inventory.stock.change.StockChangeResult;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseOrderMapper purchaseOrderMapper;

    @Mock
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private StockChangeService stockChangeService;

    private PurchaseServiceImpl purchaseService;

    @BeforeEach
    void setUp() {
        purchaseService = new PurchaseServiceImpl(
            purchaseOrderMapper,
            purchaseOrderItemMapper,
            supplierMapper,
            productMapper,
            stockChangeService
        );
    }

    @Test
    void createShouldPersistMultiItemPurchaseAndIncreaseStock() {
        when(supplierMapper.findById(1L)).thenReturn(supplier(1L, 1));
        when(productMapper.findById(10L)).thenReturn(product(10L, 1));
        when(productMapper.findById(11L)).thenReturn(product(11L, 1));
        doAnswer(invocation -> {
            PurchaseOrder order = invocation.getArgument(0);
            order.setId(100L);
            return 1;
        }).when(purchaseOrderMapper).insert(any(PurchaseOrder.class));
        when(stockChangeService.apply(any(StockChangeCommand.class)))
            .thenReturn(new StockChangeResult(10L, 0, 2))
            .thenReturn(new StockChangeResult(11L, 0, 3));

        PurchaseOrderDetailResponse response = purchaseService.create(new PurchaseOrderCreateRequest(
            1L,
            7L,
            "首批采购",
            List.of(
                new PurchaseOrderItemRequest(10L, 2, decimal("3.50"), "A"),
                new PurchaseOrderItemRequest(11L, 3, decimal("4.00"), "B")
            )
        ));

        assertNotNull(response.orderNo());
        assertTrue(response.orderNo().matches("PO\\d{18}"));
        assertEquals(100L, response.id());
        assertEquals(1L, response.supplierId());
        assertEquals(7L, response.operatorId());
        assertEquals(5, response.totalQuantity());
        assertEquals(0, decimal("19.00").compareTo(response.totalAmount()));
        assertEquals(2, response.items().size());

        ArgumentCaptor<PurchaseOrder> orderCaptor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(purchaseOrderMapper).insert(orderCaptor.capture());
        assertEquals("NORMAL", orderCaptor.getValue().getStatus());
        assertEquals(5, orderCaptor.getValue().getTotalQuantity());
        assertEquals(0, decimal("19.00").compareTo(orderCaptor.getValue().getTotalAmount()));

        ArgumentCaptor<PurchaseOrderItem> itemCaptor = ArgumentCaptor.forClass(PurchaseOrderItem.class);
        verify(purchaseOrderItemMapper, org.mockito.Mockito.times(2)).insert(itemCaptor.capture());
        assertEquals(0, decimal("7.00").compareTo(itemCaptor.getAllValues().get(0).getSubtotal()));
        assertEquals(0, decimal("12.00").compareTo(itemCaptor.getAllValues().get(1).getSubtotal()));

        ArgumentCaptor<StockChangeCommand> commandCaptor = ArgumentCaptor.forClass(StockChangeCommand.class);
        verify(stockChangeService, org.mockito.Mockito.times(2)).apply(commandCaptor.capture());
        assertEquals(SourceType.PURCHASE_IN, commandCaptor.getAllValues().get(0).sourceType());
        assertEquals(100L, commandCaptor.getAllValues().get(0).sourceId());
        assertEquals(2, commandCaptor.getAllValues().get(0).delta());
        assertEquals(3, commandCaptor.getAllValues().get(1).delta());
    }

    @Test
    void createShouldRejectEmptyItems() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
            purchaseService.create(new PurchaseOrderCreateRequest(1L, 7L, "备注", List.of()))
        );

        assertEquals(400, ex.getCode());
        assertEquals("进货明细不能为空", ex.getMessage());
        verify(purchaseOrderMapper, never()).insert(any(PurchaseOrder.class));
    }

    @Test
    void createShouldRejectDuplicateProducts() {
        when(supplierMapper.findById(1L)).thenReturn(supplier(1L, 1));
        when(productMapper.findById(10L)).thenReturn(product(10L, 1));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            purchaseService.create(new PurchaseOrderCreateRequest(
                1L,
                7L,
                "备注",
                List.of(
                    new PurchaseOrderItemRequest(10L, 1, decimal("3.50"), null),
                    new PurchaseOrderItemRequest(10L, 2, decimal("3.50"), null)
                )
            ))
        );

        assertEquals(400, ex.getCode());
        assertEquals("同一张进货单不能重复选择商品", ex.getMessage());
        verify(purchaseOrderMapper, never()).insert(any(PurchaseOrder.class));
    }

    @Test
    void createShouldRejectDisabledSupplier() {
        when(supplierMapper.findById(1L)).thenReturn(supplier(1L, 0));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            purchaseService.create(new PurchaseOrderCreateRequest(
                1L,
                7L,
                "备注",
                List.of(new PurchaseOrderItemRequest(10L, 1, decimal("3.50"), null))
            ))
        );

        assertEquals(400, ex.getCode());
        assertEquals("供应商已停用", ex.getMessage());
        verify(purchaseOrderMapper, never()).insert(any(PurchaseOrder.class));
    }

    @Test
    void createShouldRejectDisabledProduct() {
        when(supplierMapper.findById(1L)).thenReturn(supplier(1L, 1));
        when(productMapper.findById(10L)).thenReturn(product(10L, 0));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            purchaseService.create(new PurchaseOrderCreateRequest(
                1L,
                7L,
                "备注",
                List.of(new PurchaseOrderItemRequest(10L, 1, decimal("3.50"), null))
            ))
        );

        assertEquals(400, ex.getCode());
        assertEquals("商品已停用", ex.getMessage());
        verify(purchaseOrderMapper, never()).insert(any(PurchaseOrder.class));
    }

    @Test
    void listShouldReturnPagedPurchaseOrdersWithFilters() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 31);
        PurchaseOrder order = purchaseOrder(100L, "PO2026051400010001", "NORMAL");
        when(purchaseOrderMapper.count("PO202605", 1L, startDate, endDate)).thenReturn(21L);
        when(purchaseOrderMapper.findAll("PO202605", 1L, startDate, endDate, 20, 20))
            .thenReturn(List.of(order));

        PageResponse<PurchaseOrderSummaryResponse> response = purchaseService.list(new OrderQueryRequest(
            "PO202605",
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
        assertEquals("PO2026051400010001", response.items().get(0).orderNo());
    }

    @Test
    void detailShouldReturnPurchaseOrderWithItems() {
        when(purchaseOrderMapper.findById(100L)).thenReturn(purchaseOrder(100L, "PO2026051400010001", "NORMAL"));
        when(purchaseOrderItemMapper.findByOrderId(100L)).thenReturn(List.of(purchaseItem(10L, 2)));

        PurchaseOrderDetailResponse response = purchaseService.detail(100L);

        assertEquals(100L, response.id());
        assertEquals("PO2026051400010001", response.orderNo());
        assertEquals(1, response.items().size());
        assertEquals(10L, response.items().get(0).productId());
    }

    @Test
    void cancelShouldMarkOrderAndWriteReverseStockChanges() {
        PurchaseOrder order = purchaseOrder(100L, "PO2026051400010001", "NORMAL");
        when(purchaseOrderMapper.findById(100L)).thenReturn(order);
        when(purchaseOrderItemMapper.findByOrderId(100L)).thenReturn(List.of(purchaseItem(10L, 2)));

        purchaseService.cancel(100L, 7L);

        verify(purchaseOrderMapper).updateStatusById(100L, "CANCELLED");
        ArgumentCaptor<StockChangeCommand> commandCaptor = ArgumentCaptor.forClass(StockChangeCommand.class);
        verify(stockChangeService, times(1)).apply(commandCaptor.capture());
        assertEquals(SourceType.PURCHASE_IN, commandCaptor.getValue().sourceType());
        assertEquals(100L, commandCaptor.getValue().sourceId());
        assertEquals(-2, commandCaptor.getValue().delta());
        assertEquals("作废冲销：PO2026051400010001", commandCaptor.getValue().reason());
    }

    @Test
    void cancelShouldRejectAlreadyCancelledPurchaseOrder() {
        when(purchaseOrderMapper.findById(100L)).thenReturn(purchaseOrder(100L, "PO2026051400010001", "CANCELLED"));

        BusinessException ex = assertThrows(BusinessException.class, () -> purchaseService.cancel(100L, 7L));

        assertEquals(400, ex.getCode());
        assertEquals("单据已作废", ex.getMessage());
        verify(stockChangeService, never()).apply(any(StockChangeCommand.class));
    }

    private Supplier supplier(Long id, Integer status) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setName("供应商");
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

    private PurchaseOrder purchaseOrder(Long id, String orderNo, String status) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setSupplierId(1L);
        order.setOperatorId(7L);
        order.setTotalQuantity(2);
        order.setTotalAmount(decimal("7.00"));
        order.setStatus(status);
        order.setRemark("备注");
        order.setCreatedAt(LocalDateTime.of(2026, 5, 14, 10, 0));
        return order;
    }

    private PurchaseOrderItem purchaseItem(Long productId, Integer quantity) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(decimal("3.50"));
        item.setSubtotal(decimal("7.00"));
        item.setRemark("A");
        return item;
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
