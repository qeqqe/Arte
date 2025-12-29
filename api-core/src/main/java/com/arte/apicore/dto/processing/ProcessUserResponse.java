package com.arte.apicore.dto.processing;

public record ProcessUserResponse(
        boolean success,
        String message,
        ProcessedUserDataDTO processedData
) {}
