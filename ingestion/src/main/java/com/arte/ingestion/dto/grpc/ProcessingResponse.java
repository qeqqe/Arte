package com.arte.ingestion.dto.grpc;

public record ProcessingResponse(
        boolean success,
        String message
) {}