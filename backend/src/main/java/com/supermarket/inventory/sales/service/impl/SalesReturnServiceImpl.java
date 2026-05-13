package com.supermarket.inventory.sales.service.impl;

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
import com.supermarket.inventory.sales.service.SalesReturnService;
import com.supermarket.inventory.sales.vo.SalesReturnDetailResponse;
import com.supermarket.inventory.sales.vo.SalesReturnItemResponse;
import com.supermarket.inventory.sales.vo.SalesReturnSummaryResponse;
import com.supermarket.inventory.stock.change.SourceType;
import com.supermarket.inventory.stock.change.StockChangeCommand;
import com.supermarket.inventory.stock.change.StockChangeService;
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
public class SalesReturnServiceImpl implements SalesReturnService {

    private static final Integer ENABLED_STATUS = 1;
    private static final String ORDER_STATUS_NORMAL = "NORMAL";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SalesReturnOrderMapper returnOrderMapper;
    private final SalesReturnOrderItemMapper returnOrderItemMapper;
    private final CustomerMapper customerMapper;
    private final ProductMapper productMapper;
    private final StockChangeService stockChangeService;

    public SalesReturnServiceImpl(
        SalesReturnOrderMapper returnOrderMapper,
        SalesReturnOrderItemMapper returnOrderItemMapper,
        CustomerMapper customerMapper,
        ProductMapper productMapper,
        StockChangeService stockChangeService
    ) {
        this.returnOrderMapper = returnOrderMapper;
        this.returnOrderItemMapper = returnOrderItemMapper;
        this.customerMapper = customerMapper;
        this.productMapper = productMapper;
        this.stockChangeService = stockChangeService;
    }

    @Override
    @Transactional
    public SalesReturnDetailResponse create(SalesReturnCreateRequest request) {
        validateBasicRequest(request);
        Customer customer = requireEnabledCustomer(request.customerId());
        List<PreparedReturnItem> items = prepareItems(request.items());

        SalesReturnOrder order = new SalesReturnOrder();
        order.setReturnNo(generateReturnNo());
        order.setCustomerId(customer.getId());
        order.setSourceOrderId(request.sourceOrderId());
        order.setOperatorId(request.operatorId());
        order.setReason(request.reason());
        order.setStatus(ORDER_STATUS_NORMAL);
        order.setTotalQuantity(items.stream().mapToInt(item -> item.quantity).sum());
        order.setTotalAmount(items.stream()
            .map(item -> item.subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        returnOrderMapper.insert(order);

        List<SalesReturnItemResponse> itemResponses = new ArrayList<>(items.size());
        for (PreparedReturnItem item : items) {
            SalesReturnOrderItem entity = new SalesReturnOrderItem();
            entity.setReturnId(order.getId());
            entity.setProductId(item.productId);
            entity.setQuantity(item.quantity);
            entity.setUnitPrice(item.unitPrice);
            entity.setSubtotal(item.subtotal);
            entity.setRemark(item.remark);
            returnOrderItemMapper.insert(entity);

            stockChangeService.apply(new StockChangeCommand(
                item.productId,
                item.quantity,
                SourceType.SALE_RETURN_IN,
                order.getId(),
                order.getOperatorId(),
                "客户退货：" + order.getReturnNo()
            ));

            itemResponses.add(new SalesReturnItemResponse(
                entity.getId(), item.productId, item.quantity, item.unitPrice, item.subtotal, item.remark
            ));
        }

        return new SalesReturnDetailResponse(
            order.getId(),
            order.getReturnNo(),
            order.getCustomerId(),
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
    public PageResponse<SalesReturnSummaryResponse> list(OrderQueryRequest query) {
        QueryParams params = normalizeQuery(query);
        long total = returnOrderMapper.count(params.keyword, params.subjectId, params.startDate, params.endDate);
        List<SalesReturnOrder> orders = returnOrderMapper.findAll(
            params.keyword,
            params.subjectId,
            params.startDate,
            params.endDate,
            params.offset,
            params.pageSize
        );
        List<SalesReturnSummaryResponse> items = orders.stream()
            .map(this::toSummaryResponse)
            .toList();
        return new PageResponse<>(items, total, params.page, params.pageSize);
    }

    @Override
    public SalesReturnDetailResponse detail(Long id) {
        SalesReturnOrder order = requireOrder(id);
        List<SalesReturnItemResponse> items = returnOrderItemMapper.findByReturnId(id).stream()
            .map(this::toItemResponse)
            .toList();
        return toDetailResponse(order, items);
    }

    @Override
    @Transactional
    public void cancel(Long id, Long operatorId) {
        SalesReturnOrder order = requireOrder(id);
        if (ORDER_STATUS_CANCELLED.equals(order.getStatus())) {
            throw new BusinessException(400, "单据已作废");
        }

        returnOrderMapper.updateStatusById(id, ORDER_STATUS_CANCELLED);
        List<SalesReturnOrderItem> items = returnOrderItemMapper.findByReturnId(id);
        for (SalesReturnOrderItem item : items) {
            stockChangeService.apply(new StockChangeCommand(
                item.getProductId(),
                -item.getQuantity(),
                SourceType.SALE_RETURN_IN,
                order.getId(),
                operatorId,
                "作废冲销：" + order.getReturnNo()
            ));
        }
    }

    private void validateBasicRequest(SalesReturnCreateRequest request) {
        if (request == null) {
            throw new BusinessException(400, "客户退货单不能为空");
        }
        if (request.customerId() == null) {
            throw new BusinessException(400, "客户不能为空");
        }
        if (request.operatorId() == null) {
            throw new BusinessException(400, "操作人不能为空");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException(400, "客户退货明细不能为空");
        }
    }

    private Customer requireEnabledCustomer(Long customerId) {
        Customer customer = customerMapper.findById(customerId);
        if (customer == null) {
            throw new BusinessException(404, "客户不存在");
        }
        if (!ENABLED_STATUS.equals(customer.getStatus())) {
            throw new BusinessException(400, "客户已停用");
        }
        return customer;
    }

    private List<PreparedReturnItem> prepareItems(List<SalesReturnItemRequest> requests) {
        Set<Long> productIds = new HashSet<>();
        List<PreparedReturnItem> items = new ArrayList<>(requests.size());

        for (SalesReturnItemRequest request : requests) {
            validateItem(request);
            if (!productIds.add(request.productId())) {
                throw new BusinessException(400, "同一张客户退货单不能重复选择商品");
            }
            Product product = requireEnabledProduct(request.productId());
            BigDecimal subtotal = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
            items.add(new PreparedReturnItem(product.getId(), request.quantity(), request.unitPrice(), subtotal, request.remark()));
        }

        return items;
    }

    private void validateItem(SalesReturnItemRequest request) {
        if (request == null) {
            throw new BusinessException(400, "客户退货明细不能为空");
        }
        if (request.productId() == null) {
            throw new BusinessException(400, "商品不能为空");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new BusinessException(400, "客户退货数量必须大于0");
        }
        if (request.unitPrice() == null || request.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "客户退货单价不能为负");
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

    private SalesReturnOrder requireOrder(Long id) {
        SalesReturnOrder order = returnOrderMapper.findById(id);
        if (order == null) {
            throw new BusinessException(404, "客户退货单不存在");
        }
        return order;
    }

    private SalesReturnSummaryResponse toSummaryResponse(SalesReturnOrder order) {
        return new SalesReturnSummaryResponse(
            order.getId(),
            order.getReturnNo(),
            order.getCustomerId(),
            order.getSourceOrderId(),
            order.getOperatorId(),
            order.getTotalQuantity(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getReason(),
            order.getCreatedAt()
        );
    }

    private SalesReturnItemResponse toItemResponse(SalesReturnOrderItem item) {
        return new SalesReturnItemResponse(
            item.getId(),
            item.getProductId(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getSubtotal(),
            item.getRemark()
        );
    }

    private SalesReturnDetailResponse toDetailResponse(SalesReturnOrder order, List<SalesReturnItemResponse> items) {
        return new SalesReturnDetailResponse(
            order.getId(),
            order.getReturnNo(),
            order.getCustomerId(),
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
        return "SRO" + LocalDateTime.now().format(ORDER_TIME_FORMATTER) + String.format("%04d", random);
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
