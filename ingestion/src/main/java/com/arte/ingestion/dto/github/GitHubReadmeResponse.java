package com.arte.ingestion.dto.github;

public record GitHubReadmeResponse(
        String content,
        String encoding
) {}