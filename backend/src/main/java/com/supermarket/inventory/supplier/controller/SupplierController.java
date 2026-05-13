package com.supermarket.inventory.supplier.controller;

import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.supplier.dto.SupplierCreateRequest;
import com.supermarket.inventory.supplier.dto.SupplierStatusUpdateRequest;
import com.supermarket.inventory.supplier.dto.SupplierUpdateRequest;
import com.supermarket.inventory.supplier.service.SupplierService;
import com.supermarket.inventory.supplier.vo.SupplierVO;
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
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ApiResponse<List<SupplierVO>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(supplierService.list(keyword));
    }

    @PostMapping
    public ApiResponse<SupplierVO> create(@Valid @RequestBody SupplierCreateRequest request) {
        return ApiResponse.success(supplierService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SupplierVO> update(@PathVariable Long id,
                                          @Valid @RequestBody SupplierUpdateRequest request) {
        return ApiResponse.success(supplierService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody SupplierStatusUpdateRequest request) {
        supplierService.updateStatus(id, request.status());
        return ApiResponse.success();
    }
}
