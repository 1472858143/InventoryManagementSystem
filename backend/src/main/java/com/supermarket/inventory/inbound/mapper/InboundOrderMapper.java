package com.supermarket.inventory.inbound.mapper;

import com.supermarket.inventory.inbound.entity.InboundOrder;
import com.supermarket.inventory.inbound.mapper.model.InboundOrderView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InboundOrderMapper {

    int insert(InboundOrder inboundOrder);

    List<InboundOrderView> findAll();

    InboundOrderView findById(@Param("id") Long id);
}
