package com.supermarket.inventory.report.service.impl;

import com.supermarket.inventory.report.mapper.ReportMapper;
import com.supermarket.inventory.report.service.ReportService;
import com.supermarket.inventory.report.vo.StockOverviewItem;
import com.supermarket.inventory.report.vo.TrendItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    public ReportServiceImpl(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @Override
    public List<StockOverviewItem> findStockOverview() {
        return reportMapper.findStockOverview();
    }

    @Override
    public List<TrendItem> findInboundTrend() {
        return reportMapper.findInboundTrend();
    }

    @Override
    public List<TrendItem> findOutboundTrend() {
        return reportMapper.findOutboundTrend();
    }
}
