package com.supermarket.inventory.inbound.service.impl;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.inbound.dto.InboundCreateRequest;
import com.supermarket.inventory.inbound.entity.InboundOrder;
import com.supermarket.inventory.inbound.mapper.InboundOrderMapper;
import com.supermarket.inventory.inbound.mapper.model.InboundOrderView;
import com.supermarket.inventory.inbound.service.InboundService;
import com.supermarket.inventory.inbound.vo.InboundDetailResponse;
import com.supermarket.inventory.inbound.vo.InboundListItemResponse;
import com.supermarket.inventory.product.entity.Product;
import com.supermarket.inventory.product.mapper.ProductMapper;
import com.supermarket.inventory.stock.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InboundServiceImpl implements InboundService {

    private final InboundOrderMapper inboundOrderMapper;
    private final ProductMapper productMapper;
    private final StockService stockService;

    public InboundServiceImpl(
        InboundOrderMapper inboundOrderMapper,
        ProductMapper productMapper,
        StockService stockService
    ) {
        this.inboundOrderMapper = inboundOrderMapper;
        this.productMapper = productMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public InboundDetailResponse createInbound(InboundCreateRequest request) {
        validateCreateRequest(request);

        Product product = productMapper.findById(request.productId());
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        InboundOrder inboundOrder = new InboundOrder();
        inboundOrder.setProductId(request.productId());
        inboundOrder.setQuantity(request.quantity());
        inboundOrder.setOperator(request.operator());

        inboundOrderMapper.insert(inboundOrder);
        stockService.increaseStock(request.productId(), request.quantity());

        InboundOrderView createdInbound = inboundOrderMapper.findById(inboundOrder.getId());
        if (createdInbound == null) {
            throw new BusinessException(500, "入库记录回查失败");
        }

        return toInboundDetailResponse(createdInbound);
    }

    @Override
    public List<InboundListItemResponse> listInbounds() {
        List<InboundOrderView> inboundOrders = inboundOrderMapper.findAll();
        if (inboundOrders.isEmpty()) {
            return List.of();
        }

        List<InboundListItemResponse> responses = new ArrayList<>(inboundOrders.size());
        for (InboundOrderView inboundOrder : inboundOrders) {
            responses.add(toInboundListItemResponse(inboundOrder));
        }
        return responses;
    }

    private void validateCreateRequest(InboundCreateRequest request) {
        if (request.productId() == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }
        if (request.quantity() == null) {
            throw new BusinessException(400, "入库数量不能为空");
        }
        if (request.quantity() <= 0) {
            throw new BusinessException(400, "入库数量必须大于0");
        }
        if (request.operator() == null || request.operator().isBlank()) {
            throw new BusinessException(400, "操作人不能为空");
        }
    }

    private InboundListItemResponse toInboundListItemResponse(InboundOrderView inboundOrder) {
        return new InboundListItemResponse(
            inboundOrder.getId(),
            inboundOrder.getProductId(),
            inboundOrder.getProductCode(),
            inboundOrder.getProductName(),
            inboundOrder.getQuantity(),
            inboundOrder.getOperator(),
            inboundOrder.getCreateTime()
        );
    }

    private InboundDetailResponse toInboundDetailResponse(InboundOrderView inboundOrder) {
        return new InboundDetailResponse(
            inboundOrder.getId(),
            inboundOrder.getProductId(),
            inboundOrder.getProductCode(),
            inboundOrder.getProductName(),
            inboundOrder.getQuantity(),
            inboundOrder.getOperator(),
            inboundOrder.getCreateTime()
        );
    }
}
