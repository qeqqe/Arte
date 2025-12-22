package com.arte.apicore.dto.proto;

public record ResumeRequestDTO(
        String userId,
        String filename,
        byte[] content
) {
}
