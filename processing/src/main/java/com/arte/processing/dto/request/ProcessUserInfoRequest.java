package com.arte.processing.dto.request;

public record ProcessUserInfoRequest(
        String userId,
        String processingVersion
) {}