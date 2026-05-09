package com.supermarket.inventory.report.mapper;

import com.supermarket.inventory.report.vo.StockOverviewItem;
import com.supermarket.inventory.report.vo.TrendItem;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ReportMapper {
    List<StockOverviewItem> findStockOverview();
    List<TrendItem> findInboundTrend();
    List<TrendItem> findOutboundTrend();
}
