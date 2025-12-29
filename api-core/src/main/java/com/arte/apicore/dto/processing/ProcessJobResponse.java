package com.arte.apicore.dto.processing;

public record ProcessJobResponse(
        boolean success,
        String message,
        ProcessedJobDataDTO processedData
) {}
