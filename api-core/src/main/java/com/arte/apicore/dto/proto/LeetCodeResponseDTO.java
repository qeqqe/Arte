package com.arte.apicore.dto.proto;

public record LeetCodeResponseDTO(
        boolean success,
        String message,
        Integer problemSolved
) {
}
