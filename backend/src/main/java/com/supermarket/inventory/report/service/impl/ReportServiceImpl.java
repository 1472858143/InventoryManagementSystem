package com.supermarket.inventory.report.service.impl;

import com.supermarket.inventory.report.mapper.ReportMapper;
import com.supermarket.inventory.report.service.ReportService;
import com.supermarket.inventory.report.vo.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    public ReportServiceImpl(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @Override public List<StockOverviewItem> findStockOverview() { return reportMapper.findStockOverview(); }
    @Override public List<TrendItem> findInboundTrend() { return reportMapper.findInboundTrend(); }
    @Override public List<TrendItem> findOutboundTrend() { return reportMapper.findOutboundTrend(); }
    @Override public List<ShelfStatusDistributionItem> findShelfStatusDistribution() { return reportMapper.findShelfStatusDistribution(); }
    @Override public List<CategoryDistributionItem> findCategoryDistribution() { return reportMapper.findCategoryDistribution(); }
    @Override public List<RestockSuggestionItem> findRestockSuggestions() { return reportMapper.findRestockSuggestions(); }
    @Override public List<SlowMovingItem> findSlowMoving(int days) { return reportMapper.findSlowMoving(days); }
    @Override public List<SalesHotspotItem> findSalesHotspot(int days, int limit) { return reportMapper.findSalesHotspot(days, limit); }
}
