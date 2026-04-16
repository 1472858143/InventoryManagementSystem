package com.supermarket.inventory.outbound.service;

import com.supermarket.inventory.outbound.dto.OutboundCreateRequest;
import com.supermarket.inventory.outbound.vo.OutboundDetailResponse;
import com.supermarket.inventory.outbound.vo.OutboundListItemResponse;

import java.util.List;

public interface OutboundService {

    OutboundDetailResponse createOutbound(OutboundCreateRequest request);

    List<OutboundListItemResponse> listOutbounds();
}
