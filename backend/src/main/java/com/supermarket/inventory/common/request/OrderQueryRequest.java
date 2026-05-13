package com.supermarket.inventory.common.request;

import java.time.LocalDate;

public record OrderQueryRequest(
    String keyword,
    Long subjectId,
    LocalDate startDate,
    LocalDate endDate,
    Integer page,
    Integer pageSize
) {
}
