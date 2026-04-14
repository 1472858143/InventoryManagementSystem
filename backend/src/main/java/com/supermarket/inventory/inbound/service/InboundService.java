package com.supermarket.inventory.inbound.service;

import com.supermarket.inventory.inbound.dto.InboundCreateRequest;
import com.supermarket.inventory.inbound.vo.InboundDetailResponse;
import com.supermarket.inventory.inbound.vo.InboundListItemResponse;

import java.util.List;

public interface InboundService {

    InboundDetailResponse createInbound(InboundCreateRequest request);

    List<InboundListItemResponse> listInbounds();
}
