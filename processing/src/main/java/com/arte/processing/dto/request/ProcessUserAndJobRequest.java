package com.arte.processing.dto.request;

public record ProcessUserAndJobRequest(
        String userId,
        String jobId
) {}
