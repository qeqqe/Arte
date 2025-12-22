package com.arte.apicore.dto.proto;

public record ResumeResponseDTO(
        boolean success,
        String message,
        Integer wordCount
) {
}
