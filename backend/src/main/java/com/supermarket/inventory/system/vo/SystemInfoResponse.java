package com.supermarket.inventory.system.vo;

public record SystemInfoResponse(
        String systemName,
        String version,
        String frontendFramework,
        String backendFramework,
        String database,
        String deploymentEnv
) {}
