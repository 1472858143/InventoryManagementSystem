package com.supermarket.inventory.report.mapper;

import com.supermarket.inventory.report.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {
    List<StockOverviewItem> findStockOverview();
    List<TrendItem> findInboundTrend();
    List<TrendItem> findOutboundTrend();

    List<ShelfStatusDistributionItem> findShelfStatusDistribution();
    List<CategoryDistributionItem> findCategoryDistribution();
    List<RestockSuggestionItem> findRestockSuggestions();
    List<SlowMovingItem> findSlowMoving(@Param("days") int days);
    List<SalesHotspotItem> findSalesHotspot(@Param("days") int days, @Param("lim") int lim);
}
