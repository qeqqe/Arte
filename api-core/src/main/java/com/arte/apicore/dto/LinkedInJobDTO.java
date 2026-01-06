package com.arte.apicore.dto;

import java.time.Instant;
import java.util.Map;

public record LinkedInJobDTO(
        String id,
        String jobId,
        String rawContent,
        Map<String, Object> processedJobData,
        boolean isProcessed,
        Instant createdAt
) {}
