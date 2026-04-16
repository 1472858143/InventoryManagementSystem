package com.supermarket.inventory.outbound.mapper;

import com.supermarket.inventory.outbound.entity.OutboundOrder;
import com.supermarket.inventory.outbound.mapper.model.OutboundOrderView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OutboundOrderMapper {

    int insert(OutboundOrder outboundOrder);

    List<OutboundOrderView> findAll();

    OutboundOrderView findById(@Param("id") Long id);
}
