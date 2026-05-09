package com.supermarket.inventory.report.controller;

import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.report.service.ReportService;
import com.supermarket.inventory.report.vo.StockOverviewItem;
import com.supermarket.inventory.report.vo.TrendItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/stock-overview")
    public ApiResponse<List<StockOverviewItem>> stockOverview() {
        return ApiResponse.success(reportService.findStockOverview());
    }

    @GetMapping("/inbound-trend")
    public ApiResponse<List<TrendItem>> inboundTrend() {
        return ApiResponse.success(reportService.findInboundTrend());
    }

    @GetMapping("/outbound-trend")
    public ApiResponse<List<TrendItem>> outboundTrend() {
        return ApiResponse.success(reportService.findOutboundTrend());
    }
}
