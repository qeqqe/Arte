package com.arte.apicore.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record JobComparisonSummaryDTO(
        String id,
        String jobId,
        String jobTitle,
        String company,
        BigDecimal matchScore,
        Instant comparedAt
) {}
