package com.supermarket.inventory.report.controller;

import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.report.service.ReportService;
import com.supermarket.inventory.report.vo.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/shelf-status-distribution")
    public ApiResponse<List<ShelfStatusDistributionItem>> shelfStatusDistribution() {
        return ApiResponse.success(reportService.findShelfStatusDistribution());
    }

    @GetMapping("/category-distribution")
    public ApiResponse<List<CategoryDistributionItem>> categoryDistribution() {
        return ApiResponse.success(reportService.findCategoryDistribution());
    }

    @GetMapping("/restock-suggestions")
    public ApiResponse<List<RestockSuggestionItem>> restockSuggestions() {
        return ApiResponse.success(reportService.findRestockSuggestions());
    }

    @GetMapping("/slow-moving")
    public ApiResponse<List<SlowMovingItem>> slowMoving(
        @RequestParam(defaultValue = "30") int days
    ) {
        return ApiResponse.success(reportService.findSlowMoving(days));
    }

    @GetMapping("/sales-hotspot")
    public ApiResponse<List<SalesHotspotItem>> salesHotspot(
        @RequestParam(defaultValue = "30") int days,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.success(reportService.findSalesHotspot(days, limit));
    }
}
