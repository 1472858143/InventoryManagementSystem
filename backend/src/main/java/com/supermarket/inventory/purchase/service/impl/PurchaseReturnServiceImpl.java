package com.supermarket.inventory.purchase.service.impl;

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
import com.supermarket.inventory.purchase.service.PurchaseReturnService;
import com.supermarket.inventory.purchase.vo.PurchaseReturnDetailResponse;
import com.supermarket.inventory.purchase.vo.PurchaseReturnItemResponse;
import com.supermarket.inventory.purchase.vo.PurchaseReturnSummaryResponse;
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
public class PurchaseReturnServiceImpl implements PurchaseReturnService {

    private static final Integer ENABLED_STATUS = 1;
    private static final String ORDER_STATUS_NORMAL = "NORMAL";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PurchaseReturnOrderMapper returnOrderMapper;
    private final PurchaseReturnOrderItemMapper returnOrderItemMapper;
    private final SupplierMapper supplierMapper;
    private final ProductMapper productMapper;
    private final StockChangeService stockChangeService;

    public PurchaseReturnServiceImpl(
        PurchaseReturnOrderMapper returnOrderMapper,
        PurchaseReturnOrderItemMapper returnOrderItemMapper,
        SupplierMapper supplierMapper,
        ProductMapper productMapper,
        StockChangeService stockChangeService
    ) {
        this.returnOrderMapper = returnOrderMapper;
        this.returnOrderItemMapper = returnOrderItemMapper;
        this.supplierMapper = supplierMapper;
        this.productMapper = productMapper;
        this.stockChangeService = stockChangeService;
    }

    @Override
    @Transactional
    public PurchaseReturnDetailResponse create(PurchaseReturnCreateRequest request) {
        validateBasicRequest(request);
        Supplier supplier = requireEnabledSupplier(request.supplierId());
        List<PreparedReturnItem> items = prepareItems(request.items());

        PurchaseReturnOrder order = new PurchaseReturnOrder();
        order.setReturnNo(generateReturnNo());
        order.setSupplierId(supplier.getId());
        order.setSourceOrderId(request.sourceOrderId());
        order.setOperatorId(request.operatorId());
        order.setReason(request.reason());
        order.setStatus(ORDER_STATUS_NORMAL);
        order.setTotalQuantity(items.stream().mapToInt(item -> item.quantity).sum());
        order.setTotalAmount(items.stream()
            .map(item -> item.subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        returnOrderMapper.insert(order);

        List<PurchaseReturnItemResponse> itemResponses = new ArrayList<>(items.size());
        for (PreparedReturnItem item : items) {
            PurchaseReturnOrderItem entity = new PurchaseReturnOrderItem();
            entity.setReturnId(order.getId());
            entity.setProductId(item.productId);
            entity.setQuantity(item.quantity);
            entity.setUnitPrice(item.unitPrice);
            entity.setSubtotal(item.subtotal);
            entity.setRemark(item.remark);
            returnOrderItemMapper.insert(entity);

            stockChangeService.apply(new StockChangeCommand(
                item.productId,
                -item.quantity,
                SourceType.PURCHASE_RETURN_OUT,
                order.getId(),
                order.getOperatorId(),
                "采购退货：" + order.getReturnNo()
            ));

            itemResponses.add(new PurchaseReturnItemResponse(
                entity.getId(), item.productId, item.quantity, item.unitPrice, item.subtotal, item.remark
            ));
        }

        return new PurchaseReturnDetailResponse(
            order.getId(),
            order.getReturnNo(),
            order.getSupplierId(),
            order.getSourceOrderId(),
            order.getOperatorId(),
            order.getTotalQuantity(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getReason(),
            itemResponses
        );
    }

    @Override
    public PageResponse<PurchaseReturnSummaryResponse> list(OrderQueryRequest query) {
        QueryParams params = normalizeQuery(query);
        long total = returnOrderMapper.count(params.keyword, params.subjectId, params.startDate, params.endDate);
        List<PurchaseReturnOrder> orders = returnOrderMapper.findAll(
            params.keyword,
            params.subjectId,
            params.startDate,
            params.endDate,
            params.offset,
            params.pageSize
        );
        List<PurchaseReturnSummaryResponse> items = orders.stream()
            .map(this::toSummaryResponse)
            .toList();
        return new PageResponse<>(items, total, params.page, params.pageSize);
    }

    @Override
    public PurchaseReturnDetailResponse detail(Long id) {
        PurchaseReturnOrder order = requireOrder(id);
        List<PurchaseReturnItemResponse> items = returnOrderItemMapper.findByReturnId(id).stream()
            .map(this::toItemResponse)
            .toList();
        return toDetailResponse(order, items);
    }

    @Override
    @Transactional
    public void cancel(Long id, Long operatorId) {
        PurchaseReturnOrder order = requireOrder(id);
        if (ORDER_STATUS_CANCELLED.equals(order.getStatus())) {
            throw new BusinessException(400, "单据已作废");
        }

        returnOrderMapper.updateStatusById(id, ORDER_STATUS_CANCELLED);
        List<PurchaseReturnOrderItem> items = returnOrderItemMapper.findByReturnId(id);
        for (PurchaseReturnOrderItem item : items) {
            stockChangeService.apply(new StockChangeCommand(
                item.getProductId(),
                item.getQuantity(),
                SourceType.PURCHASE_RETURN_OUT,
                order.getId(),
                operatorId,
                "作废冲销：" + order.getReturnNo()
            ));
        }
    }

    private void validateBasicRequest(PurchaseReturnCreateRequest request) {
        if (request == null) {
            throw new BusinessException(400, "采购退货单不能为空");
        }
        if (request.supplierId() == null) {
            throw new BusinessException(400, "供应商不能为空");
        }
        if (request.operatorId() == null) {
            throw new BusinessException(400, "操作人不能为空");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException(400, "采购退货明细不能为空");
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

    private List<PreparedReturnItem> prepareItems(List<PurchaseReturnItemRequest> requests) {
        Set<Long> productIds = new HashSet<>();
        List<PreparedReturnItem> items = new ArrayList<>(requests.size());

        for (PurchaseReturnItemRequest request : requests) {
            validateItem(request);
            if (!productIds.add(request.productId())) {
                throw new BusinessException(400, "同一张采购退货单不能重复选择商品");
            }
            Product product = requireEnabledProduct(request.productId());
            BigDecimal subtotal = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
            items.add(new PreparedReturnItem(product.getId(), request.quantity(), request.unitPrice(), subtotal, request.remark()));
        }

        return items;
    }

    private void validateItem(PurchaseReturnItemRequest request) {
        if (request == null) {
            throw new BusinessException(400, "采购退货明细不能为空");
        }
        if (request.productId() == null) {
            throw new BusinessException(400, "商品不能为空");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new BusinessException(400, "采购退货数量必须大于0");
        }
        if (request.unitPrice() == null || request.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "采购退货单价不能为负");
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

    private PurchaseReturnOrder requireOrder(Long id) {
        PurchaseReturnOrder order = returnOrderMapper.findById(id);
        if (order == null) {
            throw new BusinessException(404, "采购退货单不存在");
        }
        return order;
    }

    private PurchaseReturnSummaryResponse toSummaryResponse(PurchaseReturnOrder order) {
        return new PurchaseReturnSummaryResponse(
            order.getId(),
            order.getReturnNo(),
            order.getSupplierId(),
            order.getSourceOrderId(),
            order.getOperatorId(),
            order.getTotalQuantity(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getReason(),
            order.getCreatedAt()
        );
    }

    private PurchaseReturnItemResponse toItemResponse(PurchaseReturnOrderItem item) {
        return new PurchaseReturnItemResponse(
            item.getId(),
            item.getProductId(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getSubtotal(),
            item.getRemark()
        );
    }

    private PurchaseReturnDetailResponse toDetailResponse(PurchaseReturnOrder order, List<PurchaseReturnItemResponse> items) {
        return new PurchaseReturnDetailResponse(
            order.getId(),
            order.getReturnNo(),
            order.getSupplierId(),
            order.getSourceOrderId(),
            order.getOperatorId(),
            order.getTotalQuantity(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getReason(),
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

    private String generateReturnNo() {
        int random = ThreadLocalRandom.current().nextInt(10_000);
        return "PRO" + LocalDateTime.now().format(ORDER_TIME_FORMATTER) + String.format("%04d", random);
    }

    private record PreparedReturnItem(
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
