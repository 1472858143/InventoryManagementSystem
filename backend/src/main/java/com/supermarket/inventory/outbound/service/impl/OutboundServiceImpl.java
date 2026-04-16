package com.supermarket.inventory.outbound.service.impl;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.outbound.dto.OutboundCreateRequest;
import com.supermarket.inventory.outbound.entity.OutboundOrder;
import com.supermarket.inventory.outbound.mapper.OutboundOrderMapper;
import com.supermarket.inventory.outbound.mapper.model.OutboundOrderView;
import com.supermarket.inventory.outbound.service.OutboundService;
import com.supermarket.inventory.outbound.vo.OutboundDetailResponse;
import com.supermarket.inventory.outbound.vo.OutboundListItemResponse;
import com.supermarket.inventory.product.entity.Product;
import com.supermarket.inventory.product.mapper.ProductMapper;
import com.supermarket.inventory.stock.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OutboundServiceImpl implements OutboundService {

    private final OutboundOrderMapper outboundOrderMapper;
    private final ProductMapper productMapper;
    private final StockService stockService;

    public OutboundServiceImpl(
        OutboundOrderMapper outboundOrderMapper,
        ProductMapper productMapper,
        StockService stockService
    ) {
        this.outboundOrderMapper = outboundOrderMapper;
        this.productMapper = productMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public OutboundDetailResponse createOutbound(OutboundCreateRequest request) {
        validateCreateRequest(request);

        Product product = productMapper.findById(request.productId());
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        OutboundOrder outboundOrder = new OutboundOrder();
        outboundOrder.setProductId(request.productId());
        outboundOrder.setQuantity(request.quantity());
        outboundOrder.setOperator(request.operator());

        outboundOrderMapper.insert(outboundOrder);
        stockService.decreaseStock(request.productId(), request.quantity());

        OutboundOrderView createdOutbound = outboundOrderMapper.findById(outboundOrder.getId());
        if (createdOutbound == null) {
            throw new BusinessException(500, "出库记录回查失败");
        }

        return toOutboundDetailResponse(createdOutbound);
    }

    @Override
    public List<OutboundListItemResponse> listOutbounds() {
        List<OutboundOrderView> outboundOrders = outboundOrderMapper.findAll();
        if (outboundOrders.isEmpty()) {
            return List.of();
        }

        List<OutboundListItemResponse> responses = new ArrayList<>(outboundOrders.size());
        for (OutboundOrderView outboundOrder : outboundOrders) {
            responses.add(toOutboundListItemResponse(outboundOrder));
        }
        return responses;
    }

    private void validateCreateRequest(OutboundCreateRequest request) {
        if (request.productId() == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }
        if (request.quantity() == null) {
            throw new BusinessException(400, "出库数量不能为空");
        }
        if (request.quantity() <= 0) {
            throw new BusinessException(400, "出库数量必须大于0");
        }
        if (request.operator() == null || request.operator().isBlank()) {
            throw new BusinessException(400, "操作人不能为空");
        }
    }

    private OutboundListItemResponse toOutboundListItemResponse(OutboundOrderView outboundOrder) {
        return new OutboundListItemResponse(
            outboundOrder.getId(),
            outboundOrder.getProductId(),
            outboundOrder.getProductCode(),
            outboundOrder.getProductName(),
            outboundOrder.getQuantity(),
            outboundOrder.getOperator(),
            outboundOrder.getCreateTime()
        );
    }

    private OutboundDetailResponse toOutboundDetailResponse(OutboundOrderView outboundOrder) {
        return new OutboundDetailResponse(
            outboundOrder.getId(),
            outboundOrder.getProductId(),
            outboundOrder.getProductCode(),
            outboundOrder.getProductName(),
            outboundOrder.getQuantity(),
            outboundOrder.getOperator(),
            outboundOrder.getCreateTime()
        );
    }
}
