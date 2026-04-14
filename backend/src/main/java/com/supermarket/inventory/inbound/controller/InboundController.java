package com.supermarket.inventory.inbound.controller;

import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.inbound.dto.InboundCreateRequest;
import com.supermarket.inventory.inbound.service.InboundService;
import com.supermarket.inventory.inbound.vo.InboundDetailResponse;
import com.supermarket.inventory.inbound.vo.InboundListItemResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inbounds")
public class InboundController {

    private final InboundService inboundService;

    public InboundController(InboundService inboundService) {
        this.inboundService = inboundService;
    }

    @PostMapping
    public ApiResponse<InboundDetailResponse> createInbound(@Valid @RequestBody InboundCreateRequest request) {
        return ApiResponse.success(inboundService.createInbound(request));
    }

    @GetMapping
    public ApiResponse<List<InboundListItemResponse>> listInbounds() {
        return ApiResponse.success(inboundService.listInbounds());
    }
}
