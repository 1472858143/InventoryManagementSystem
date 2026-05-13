package com.supermarket.inventory.supplier.service;

import com.supermarket.inventory.supplier.dto.SupplierCreateRequest;
import com.supermarket.inventory.supplier.dto.SupplierUpdateRequest;
import com.supermarket.inventory.supplier.vo.SupplierVO;

import java.util.List;

public interface SupplierService {

    SupplierVO create(SupplierCreateRequest request);

    List<SupplierVO> list(String keyword);

    SupplierVO update(Long id, SupplierUpdateRequest request);

    void updateStatus(Long id, Integer status);
}
