package com.supermarket.inventory.customer.service;

import com.supermarket.inventory.customer.dto.CustomerCreateRequest;
import com.supermarket.inventory.customer.dto.CustomerUpdateRequest;
import com.supermarket.inventory.customer.vo.CustomerVO;

import java.util.List;

public interface CustomerService {

    CustomerVO create(CustomerCreateRequest request);

    List<CustomerVO> list(String keyword);

    CustomerVO update(Long id, CustomerUpdateRequest request);

    void updateStatus(Long id, Integer status);
}
