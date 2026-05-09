package com.supermarket.inventory.system.service.impl;

import com.supermarket.inventory.system.service.SystemService;
import com.supermarket.inventory.system.vo.SystemInfoResponse;
import org.springframework.stereotype.Service;

@Service
public class SystemServiceImpl implements SystemService {

    @Override
    public SystemInfoResponse getSystemInfo() {
        return new SystemInfoResponse(
                "超市库存管理系统",
                "V1.0.0",
                "Vue 3 + Element Plus",
                "Spring Boot",
                "MySQL 8.0.44",
                "华为云 ECS"
        );
    }
}
