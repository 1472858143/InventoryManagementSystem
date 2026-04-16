package com.supermarket.inventory.outbound.controller;

import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.outbound.dto.OutboundCreateRequest;
import com.supermarket.inventory.outbound.service.OutboundService;
import com.supermarket.inventory.outbound.vo.OutboundDetailResponse;
import com.supermarket.inventory.outbound.vo.OutboundListItemResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/outbounds")
public class OutboundController {

    private final OutboundService outboundService;

    public OutboundController(OutboundService outboundService) {
        this.outboundService = outboundService;
    }

    @PostMapping
    public ApiResponse<OutboundDetailResponse> createOutbound(@Valid @RequestBody OutboundCreateRequest request) {
        return ApiResponse.success(outboundService.createOutbound(request));
    }

    @GetMapping
    public ApiResponse<List<OutboundListItemResponse>> listOutbounds() {
        return ApiResponse.success(outboundService.listOutbounds());
    }
}
