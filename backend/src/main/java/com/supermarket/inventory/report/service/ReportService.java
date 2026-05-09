package com.supermarket.inventory.report.service;

import com.supermarket.inventory.report.vo.StockOverviewItem;
import com.supermarket.inventory.report.vo.TrendItem;

import java.util.List;

public interface ReportService {
    List<StockOverviewItem> findStockOverview();
    List<TrendItem> findInboundTrend();
    List<TrendItem> findOutboundTrend();
}
