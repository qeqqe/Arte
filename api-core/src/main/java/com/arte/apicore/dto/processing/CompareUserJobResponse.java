package com.arte.apicore.dto.processing;

public record CompareUserJobResponse(
        boolean success,
        String message,
        UserJobComparisonDTO comparison
) {}
