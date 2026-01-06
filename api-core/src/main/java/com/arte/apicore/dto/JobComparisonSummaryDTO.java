package com.arte.apicore.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record JobComparisonSummaryDTO(
        String id,
        String jobId,
        String jobTitle,
        String company,
        BigDecimal matchScore,
        Instant comparedAt
) {}
