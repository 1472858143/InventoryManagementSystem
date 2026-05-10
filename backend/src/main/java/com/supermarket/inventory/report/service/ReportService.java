package com.supermarket.inventory.report.service;

import com.supermarket.inventory.report.vo.*;

import java.util.List;

public interface ReportService {
    List<StockOverviewItem> findStockOverview();
    List<TrendItem> findInboundTrend();
    List<TrendItem> findOutboundTrend();
    List<ShelfStatusDistributionItem> findShelfStatusDistribution();
    List<CategoryDistributionItem> findCategoryDistribution();
    List<RestockSuggestionItem> findRestockSuggestions();
    List<SlowMovingItem> findSlowMoving(int days);
    List<SalesHotspotItem> findSalesHotspot(int days, int limit);
}
