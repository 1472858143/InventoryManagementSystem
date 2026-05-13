package com.supermarket.inventory.sales.service.impl;

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
import com.supermarket.inventory.sales.service.SalesService;
import com.supermarket.inventory.sales.vo.SalesOrderDetailResponse;
import com.supermarket.inventory.sales.vo.SalesOrderItemResponse;
import com.supermarket.inventory.sales.vo.SalesOrderSummaryResponse;
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
public class SalesServiceImpl implements SalesService {

    private static final Integer ENABLED_STATUS = 1;
    private static final String ORDER_STATUS_NORMAL = "NORMAL";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;
    private final CustomerMapper customerMapper;
    private final ProductMapper productMapper;
    private final StockChangeService stockChangeService;

    public SalesServiceImpl(
        SalesOrderMapper salesOrderMapper,
        SalesOrderItemMapper salesOrderItemMapper,
        CustomerMapper customerMapper,
        ProductMapper productMapper,
        StockChangeService stockChangeService
    ) {
        this.salesOrderMapper = salesOrderMapper;
        this.salesOrderItemMapper = salesOrderItemMapper;
        this.customerMapper = customerMapper;
        this.productMapper = productMapper;
        this.stockChangeService = stockChangeService;
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse create(SalesOrderCreateRequest request) {
        validateBasicRequest(request);
        Customer customer = requireEnabledCustomer(request.customerId());
        List<PreparedItem> items = prepareItems(request.items());

        SalesOrder order = new SalesOrder();
        order.setOrderNo(generateOrderNo());
        order.setCustomerId(customer.getId());
        order.setOperatorId(request.operatorId());
        order.setRemark(request.remark());
        order.setStatus(ORDER_STATUS_NORMAL);
        order.setTotalQuantity(items.stream().mapToInt(item -> item.quantity).sum());
        order.setTotalAmount(items.stream()
            .map(item -> item.subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        salesOrderMapper.insert(order);

        List<SalesOrderItemResponse> itemResponses = new ArrayList<>(items.size());
        for (PreparedItem item : items) {
            SalesOrderItem entity = new SalesOrderItem();
            entity.setOrderId(order.getId());
            entity.setProductId(item.productId);
            entity.setQuantity(item.quantity);
            entity.setUnitPrice(item.unitPrice);
            entity.setSubtotal(item.subtotal);
            entity.setRemark(item.remark);
            salesOrderItemMapper.insert(entity);

            stockChangeService.apply(new StockChangeCommand(
                item.productId,
                -item.quantity,
                SourceType.SALE_OUT,
                order.getId(),
                order.getOperatorId(),
                "销售出库：" + order.getOrderNo()
            ));

            itemResponses.add(new SalesOrderItemResponse(
                entity.getId(), item.productId, item.quantity, item.unitPrice, item.subtotal, item.remark
            ));
        }

        return new SalesOrderDetailResponse(
            order.getId(),
            order.getOrderNo(),
            order.getCustomerId(),
            order.getOperatorId(),
            order.getTotalQuantity(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getRemark(),
            itemResponses
        );
    }

    @Override
    public PageResponse<SalesOrderSummaryResponse> list(OrderQueryRequest query) {
        QueryParams params = normalizeQuery(query);
        long total = salesOrderMapper.count(params.keyword, params.subjectId, params.startDate, params.endDate);
        List<SalesOrder> orders = salesOrderMapper.findAll(
            params.keyword,
            params.subjectId,
            params.startDate,
            params.endDate,
            params.offset,
            params.pageSize
        );
        List<SalesOrderSummaryResponse> items = orders.stream()
            .map(this::toSummaryResponse)
            .toList();
        return new PageResponse<>(items, total, params.page, params.pageSize);
    }

    @Override
    public SalesOrderDetailResponse detail(Long id) {
        SalesOrder order = requireOrder(id);
        List<SalesOrderItemResponse> items = salesOrderItemMapper.findByOrderId(id).stream()
            .map(this::toItemResponse)
            .toList();
        return toDetailResponse(order, items);
    }

    @Override
    @Transactional
    public void cancel(Long id, Long operatorId) {
        SalesOrder order = requireOrder(id);
        if (ORDER_STATUS_CANCELLED.equals(order.getStatus())) {
            throw new BusinessException(400, "单据已作废");
        }

        salesOrderMapper.updateStatusById(id, ORDER_STATUS_CANCELLED);
        List<SalesOrderItem> items = salesOrderItemMapper.findByOrderId(id);
        for (SalesOrderItem item : items) {
            stockChangeService.apply(new StockChangeCommand(
                item.getProductId(),
                item.getQuantity(),
                SourceType.SALE_OUT,
                order.getId(),
                operatorId,
                "作废冲销：" + order.getOrderNo()
            ));
        }
    }

    private void validateBasicRequest(SalesOrderCreateRequest request) {
        if (request == null) {
            throw new BusinessException(400, "销售单不能为空");
        }
        if (request.customerId() == null) {
            throw new BusinessException(400, "客户不能为空");
        }
        if (request.operatorId() == null) {
            throw new BusinessException(400, "操作人不能为空");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException(400, "销售明细不能为空");
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

    private List<PreparedItem> prepareItems(List<SalesOrderItemRequest> requests) {
        Set<Long> productIds = new HashSet<>();
        List<PreparedItem> items = new ArrayList<>(requests.size());

        for (SalesOrderItemRequest request : requests) {
            validateItem(request);
            if (!productIds.add(request.productId())) {
                throw new BusinessException(400, "同一张销售单不能重复选择商品");
            }
            Product product = requireEnabledProduct(request.productId());
            BigDecimal subtotal = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
            items.add(new PreparedItem(product.getId(), request.quantity(), request.unitPrice(), subtotal, request.remark()));
        }

        return items;
    }

    private void validateItem(SalesOrderItemRequest request) {
        if (request == null) {
            throw new BusinessException(400, "销售明细不能为空");
        }
        if (request.productId() == null) {
            throw new BusinessException(400, "商品不能为空");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new BusinessException(400, "销售数量必须大于0");
        }
        if (request.unitPrice() == null || request.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "销售单价不能为负");
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

    private SalesOrder requireOrder(Long id) {
        SalesOrder order = salesOrderMapper.findById(id);
        if (order == null) {
            throw new BusinessException(404, "销售单不存在");
        }
        return order;
    }

    private SalesOrderSummaryResponse toSummaryResponse(SalesOrder order) {
        return new SalesOrderSummaryResponse(
            order.getId(),
            order.getOrderNo(),
            order.getCustomerId(),
            order.getOperatorId(),
            order.getTotalQuantity(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getRemark(),
            order.getCreatedAt()
        );
    }

    private SalesOrderItemResponse toItemResponse(SalesOrderItem item) {
        return new SalesOrderItemResponse(
            item.getId(),
            item.getProductId(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getSubtotal(),
            item.getRemark()
        );
    }

    private SalesOrderDetailResponse toDetailResponse(SalesOrder order, List<SalesOrderItemResponse> items) {
        return new SalesOrderDetailResponse(
            order.getId(),
            order.getOrderNo(),
            order.getCustomerId(),
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
        return "SO" + LocalDateTime.now().format(ORDER_TIME_FORMATTER) + String.format("%04d", random);
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
