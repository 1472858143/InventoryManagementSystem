package com.supermarket.inventory.supplier.service.impl;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.supplier.dto.SupplierCreateRequest;
import com.supermarket.inventory.supplier.dto.SupplierUpdateRequest;
import com.supermarket.inventory.supplier.entity.Supplier;
import com.supermarket.inventory.supplier.mapper.SupplierMapper;
import com.supermarket.inventory.supplier.service.SupplierService;
import com.supermarket.inventory.supplier.vo.SupplierVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SupplierServiceImpl implements SupplierService {

    private static final int DEFAULT_STATUS = 1;
    private static final DateTimeFormatter CODE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SupplierMapper supplierMapper;

    public SupplierServiceImpl(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }

    @Override
    public SupplierVO create(SupplierCreateRequest request) {
        String code = normalizeCode(request.code());
        boolean autoGenerate = (code == null);

        Supplier supplier = new Supplier();
        supplier.setName(request.name().trim());
        supplier.setContactPerson(request.contactPerson());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());
        supplier.setRemark(request.remark());
        supplier.setStatus(DEFAULT_STATUS);

        insertSupplier(supplier, code, autoGenerate);
        return toVO(supplierMapper.findById(supplier.getId()));
    }

    @Override
    public List<SupplierVO> list(String keyword) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return supplierMapper.findAll(kw).stream().map(this::toVO).toList();
    }

    @Override
    public SupplierVO update(Long id, SupplierUpdateRequest request) {
        Supplier existing = supplierMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(404, "供应商不存在");
        }
        supplierMapper.updateById(id, request.name().trim(), request.contactPerson(),
                request.phone(), request.address(), request.remark());
        return toVO(supplierMapper.findById(id));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (supplierMapper.findById(id) == null) {
            throw new BusinessException(404, "供应商不存在");
        }
        supplierMapper.updateStatusById(id, status);
    }

    private void insertSupplier(Supplier supplier, String code, boolean autoGenerate) {
        int maxAttempts = autoGenerate ? 2 : 1;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            supplier.setCode(autoGenerate ? generateCode() : code);
            try {
                supplierMapper.insert(supplier);
                return;
            } catch (DuplicateKeyException ex) {
                if (!autoGenerate) {
                    throw new BusinessException(400, "供应商编码已存在");
                }
                if (attempt == maxAttempts) {
                    throw new BusinessException(500, "供应商编号生成失败，请重试");
                }
            }
        }
    }

    private String generateCode() {
        int random = ThreadLocalRandom.current().nextInt(10_000);
        return "S" + LocalDateTime.now().format(CODE_TIME_FORMATTER) + String.format("%04d", random);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return code.trim();
    }

    private SupplierVO toVO(Supplier s) {
        return new SupplierVO(
            s.getId(), s.getCode(), s.getName(), s.getContactPerson(),
            s.getPhone(), s.getAddress(), s.getRemark(),
            s.getStatus(), s.getCreatedAt()
        );
    }
}
