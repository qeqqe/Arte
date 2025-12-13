package com.arte.ingestion.dto.github;

public record GitHubReadmeResponse(
        String content,
        String encoding  // Base64 will be decoded to normal text
) {}