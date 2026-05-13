package com.supermarket.inventory.purchase.service.impl;

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
import com.supermarket.inventory.purchase.service.PurchaseService;
import com.supermarket.inventory.purchase.vo.PurchaseOrderDetailResponse;
import com.supermarket.inventory.purchase.vo.PurchaseOrderItemResponse;
import com.supermarket.inventory.purchase.vo.PurchaseOrderSummaryResponse;
import com.supermarket.inventory.stock.change.SourceType;
import com.supermarket.inventory.stock.change.StockChangeCommand;
import com.supermarket.inventory.stock.change.StockChangeService;
import com.supermarket.inventory.supplier.entity.Supplier;
import com.supermarket.inventory.supplier.mapper.SupplierMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private static final Integer ENABLED_STATUS = 1;
    private static final String ORDER_STATUS_NORMAL = "NORMAL";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final SupplierMapper supplierMapper;
    private final ProductMapper productMapper;
    private final StockChangeService stockChangeService;

    public PurchaseServiceImpl(
        PurchaseOrderMapper purchaseOrderMapper,
        PurchaseOrderItemMapper purchaseOrderItemMapper,
        SupplierMapper supplierMapper,
        ProductMapper productMapper,
        StockChangeService stockChangeService
    ) {
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.purchaseOrderItemMapper = purchaseOrderItemMapper;
        this.supplierMapper = supplierMapper;
        this.productMapper = productMapper;
        this.stockChangeService = stockChangeService;
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse create(PurchaseOrderCreateRequest request) {
        validateBasicRequest(request);
        Supplier supplier = requireEnabledSupplier(request.supplierId());
        List<PreparedItem> items = prepareItems(request.items());

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo());
        order.setSupplierId(supplier.getId());
        order.setOperatorId(request.operatorId());
        order.setRemark(request.remark());
        order.setStatus(ORDER_STATUS_NORMAL);
        order.setTotalQuantity(items.stream().mapToInt(item -> item.quantity).sum());
        order.setTotalAmount(items.stream()
            .map(item -> item.subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        purchaseOrderMapper.insert(order);

        List<PurchaseOrderItemResponse> itemResponses = new ArrayList<>(items.size());
        for (PreparedItem item : items) {
            PurchaseOrderItem entity = new PurchaseOrderItem();
            entity.setOrderId(order.getId());
            entity.setProductId(item.productId);
            entity.setQuantity(item.quantity);
            entity.setUnitPrice(item.unitPrice);
            entity.setSubtotal(item.subtotal);
            entity.setRemark(item.remark);
            purchaseOrderItemMapper.insert(entity);

            stockChangeService.apply(new StockChangeCommand(
                item.productId,
                item.quantity,
                SourceType.PURCHASE_IN,
                order.getId(),
                order.getOperatorId(),
                "进货入库：" + order.getOrderNo()
            ));

            itemResponses.add(new PurchaseOrderItemResponse(
                entity.getId(), item.productId, item.quantity, item.unitPrice, item.subtotal, item.remark
            ));
        }

        return new PurchaseOrderDetailResponse(
            order.getId(),
            order.getOrderNo(),
            order.getSupplierId(),
            order.getOperatorId(),
            order.getTotalQuantity(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getRemark(),
            itemResponses
        );
    }

    @Override
    public PageResponse<PurchaseOrderSummaryResponse> list(OrderQueryRequest query) {
        QueryParams params = normalizeQuery(query);
        long total = purchaseOrderMapper.count(params.keyword, params.subjectId, params.startDate, params.endDate);
        List<PurchaseOrder> orders = purchaseOrderMapper.findAll(
            params.keyword,
            params.subjectId,
            params.startDate,
            params.endDate,
            params.offset,
            params.pageSize
        );
        List<PurchaseOrderSummaryResponse> items = orders.stream()
            .map(this::toSummaryResponse)
            .toList();
        return new PageResponse<>(items, total, params.page, params.pageSize);
    }

    @Override
    public PurchaseOrderDetailResponse detail(Long id) {
        PurchaseOrder order = requireOrder(id);
        List<PurchaseOrderItemResponse> items = purchaseOrderItemMapper.findByOrderId(id).stream()
            .map(this::toItemResponse)
            .toList();
        return toDetailResponse(order, items);
    }

    @Override
    @Transactional
    public void cancel(Long id, Long operatorId) {
        PurchaseOrder order = requireOrder(id);
        if (ORDER_STATUS_CANCELLED.equals(order.getStatus())) {
            throw new BusinessException(400, "单据已作废");
        }

        purchaseOrderMapper.updateStatusById(id, ORDER_STATUS_CANCELLED);
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.findByOrderId(id);
        for (PurchaseOrderItem item : items) {
            stockChangeService.apply(new StockChangeCommand(
                item.getProductId(),
                -item.getQuantity(),
                SourceType.PURCHASE_IN,
                order.getId(),
                operatorId,
                "作废冲销：" + order.getOrderNo()
            ));
        }
    }

    private void validateBasicRequest(PurchaseOrderCreateRequest request) {
        if (request == null) {
            throw new BusinessException(400, "进货单不能为空");
        }
        if (request.supplierId() == null) {
            throw new BusinessException(400, "供应商不能为空");
        }
        if (request.operatorId() == null) {
            throw new BusinessException(400, "操作人不能为空");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException(400, "进货明细不能为空");
        }
    }

    private Supplier requireEnabledSupplier(Long supplierId) {
        Supplier supplier = supplierMapper.findById(supplierId);
        if (supplier == null) {
            throw new BusinessException(404, "供应商不存在");
        }
        if (!ENABLED_STATUS.equals(supplier.getStatus())) {
            throw new BusinessException(400, "供应商已停用");
        }
        return supplier;
    }

    private List<PreparedItem> prepareItems(List<PurchaseOrderItemRequest> requests) {
        Set<Long> productIds = new HashSet<>();
        List<PreparedItem> items = new ArrayList<>(requests.size());

        for (PurchaseOrderItemRequest request : requests) {
            validateItem(request);
            if (!productIds.add(request.productId())) {
                throw new BusinessException(400, "同一张进货单不能重复选择商品");
            }
            Product product = requireEnabledProduct(request.productId());
            BigDecimal subtotal = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
            items.add(new PreparedItem(product.getId(), request.quantity(), request.unitPrice(), subtotal, request.remark()));
        }

        return items;
    }

    private void validateItem(PurchaseOrderItemRequest request) {
        if (request == null) {
            throw new BusinessException(400, "进货明细不能为空");
        }
        if (request.productId() == null) {
            throw new BusinessException(400, "商品不能为空");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new BusinessException(400, "进货数量必须大于0");
        }
        if (request.unitPrice() == null || request.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "进货单价不能为负");
        }
    }

    private Product requireEnabledProduct(Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!ENABLED_STATUS.equals(product.getStatus())) {
            throw new BusinessException(400, "商品已停用");
        }
        return product;
    }

    private PurchaseOrder requireOrder(Long id) {
        PurchaseOrder order = purchaseOrderMapper.findById(id);
        if (order == null) {
            throw new BusinessException(404, "进货单不存在");
        }
        return order;
    }

    private PurchaseOrderSummaryResponse toSummaryResponse(PurchaseOrder order) {
        return new PurchaseOrderSummaryResponse(
            order.getId(),
            order.getOrderNo(),
            order.getSupplierId(),
            order.getOperatorId(),
            order.getTotalQuantity(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getRemark(),
            order.getCreatedAt()
        );
    }

    private PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
        return new PurchaseOrderItemResponse(
            item.getId(),
            item.getProductId(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getSubtotal(),
            item.getRemark()
        );
    }

    private PurchaseOrderDetailResponse toDetailResponse(PurchaseOrder order, List<PurchaseOrderItemResponse> items) {
        return new PurchaseOrderDetailResponse(
            order.getId(),
            order.getOrderNo(),
            order.getSupplierId(),
            order.getOperatorId(),
            order.getTotalQuantity(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getRemark(),
            items
        );
    }

    private QueryParams normalizeQuery(OrderQueryRequest query) {
        String keyword = query == null || query.keyword() == null || query.keyword().isBlank()
            ? null
            : query.keyword().trim();
        Long subjectId = query == null ? null : query.subjectId();
        LocalDate startDate = query == null ? null : query.startDate();
        LocalDate endDate = query == null ? null : query.endDate();
        int page = query == null || query.page() == null || query.page() < 1 ? 1 : query.page();
        int pageSize = query == null || query.pageSize() == null || query.pageSize() < 1 ? 10 : Math.min(query.pageSize(), 100);
        return new QueryParams(keyword, subjectId, startDate, endDate, page, pageSize, (page - 1) * pageSize);
    }

    private String generateOrderNo() {
        int random = ThreadLocalRandom.current().nextInt(10_000);
        return "PO" + LocalDateTime.now().format(ORDER_TIME_FORMATTER) + String.format("%04d", random);
    }

    private record PreparedItem(
        Long productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String remark
    ) {
    }

    private record QueryParams(
        String keyword,
        Long subjectId,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int pageSize,
        int offset
    ) {
    }
}
