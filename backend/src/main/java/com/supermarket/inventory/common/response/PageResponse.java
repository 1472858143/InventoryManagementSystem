package com.supermarket.inventory.common.response;

import java.util.List;

public record PageResponse<T>(
    List<T> items,
    long total,
    int page,
    int pageSize
) {
}
