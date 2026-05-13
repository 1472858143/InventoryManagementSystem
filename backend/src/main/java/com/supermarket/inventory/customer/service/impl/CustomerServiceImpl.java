package com.supermarket.inventory.customer.service.impl;

import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.customer.dto.CustomerCreateRequest;
import com.supermarket.inventory.customer.dto.CustomerUpdateRequest;
import com.supermarket.inventory.customer.entity.Customer;
import com.supermarket.inventory.customer.mapper.CustomerMapper;
import com.supermarket.inventory.customer.service.CustomerService;
import com.supermarket.inventory.customer.vo.CustomerVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final int DEFAULT_STATUS = 1;
    private static final DateTimeFormatter CODE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional
    public CustomerVO create(CustomerCreateRequest request) {
        String code = normalizeCode(request.code());
        boolean autoGenerate = (code == null);

        if (!autoGenerate) {
            if (customerMapper.findByCode(code) != null) {
                throw new BusinessException(400, "客户编码已存在");
            }
        }

        Customer customer = new Customer();
        customer.setName(request.name().trim());
        customer.setContactPerson(request.contactPerson());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());
        customer.setRemark(request.remark());
        customer.setStatus(DEFAULT_STATUS);

        insertCustomer(customer, code, autoGenerate);
        return toVO(customerMapper.findById(customer.getId()));
    }

    @Override
    public List<CustomerVO> list(String keyword) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return customerMapper.findAll(kw).stream().map(this::toVO).toList();
    }

    @Override
    public CustomerVO update(Long id, CustomerUpdateRequest request) {
        Customer existing = customerMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(404, "客户不存在");
        }
        customerMapper.updateById(id, request.name().trim(), request.contactPerson(),
                request.phone(), request.address(), request.remark());
        return toVO(customerMapper.findById(id));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (customerMapper.findById(id) == null) {
            throw new BusinessException(404, "客户不存在");
        }
        customerMapper.updateStatusById(id, status);
    }

    private void insertCustomer(Customer customer, String code, boolean autoGenerate) {
        int maxAttempts = autoGenerate ? 2 : 1;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            customer.setCode(autoGenerate ? generateCode() : code);
            try {
                customerMapper.insert(customer);
                return;
            } catch (DuplicateKeyException ex) {
                if (!autoGenerate) {
                    throw new BusinessException(400, "客户编码已存在");
                }
                if (attempt == maxAttempts) {
                    throw new BusinessException(500, "客户编号生成失败，请重试");
                }
            }
        }
    }

    private String generateCode() {
        int random = ThreadLocalRandom.current().nextInt(10_000);
        return "C" + LocalDateTime.now().format(CODE_TIME_FORMATTER) + String.format("%04d", random);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return code.trim();
    }

    private CustomerVO toVO(Customer c) {
        return new CustomerVO(
            c.getId(), c.getCode(), c.getName(), c.getContactPerson(),
            c.getPhone(), c.getAddress(), c.getRemark(),
            c.getStatus(), c.getCreatedAt(), c.getUpdatedAt()
        );
    }
}
