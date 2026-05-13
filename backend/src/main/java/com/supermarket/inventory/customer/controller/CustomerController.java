package com.supermarket.inventory.customer.controller;

import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.customer.dto.CustomerCreateRequest;
import com.supermarket.inventory.customer.dto.CustomerStatusUpdateRequest;
import com.supermarket.inventory.customer.dto.CustomerUpdateRequest;
import com.supermarket.inventory.customer.service.CustomerService;
import com.supermarket.inventory.customer.vo.CustomerVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ApiResponse<List<CustomerVO>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(customerService.list(keyword));
    }

    @PostMapping
    public ApiResponse<CustomerVO> create(@Valid @RequestBody CustomerCreateRequest request) {
        return ApiResponse.success(customerService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerVO> update(@PathVariable Long id,
                                          @Valid @RequestBody CustomerUpdateRequest request) {
        return ApiResponse.success(customerService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody CustomerStatusUpdateRequest request) {
        customerService.updateStatus(id, request.status());
        return ApiResponse.success();
    }
}
