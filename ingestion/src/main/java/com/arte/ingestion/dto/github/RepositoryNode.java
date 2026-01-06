package com.arte.ingestion.dto.github;


public record RepositoryNode(
        String name,
        String description,
        String url,
        Integer stargazerCount,
        Integer forkCount,
        PrimaryLanguage primaryLanguage,
        RepositoryTopics repositoryTopics
) {}