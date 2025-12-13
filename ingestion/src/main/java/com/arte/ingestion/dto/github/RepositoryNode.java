package com.arte.ingestion.dto.github;


import java.util.List;

public record RepositoryNode(
        String name,
        String description,
        String url,
        Integer stargazerCount,
        Integer forkCount,
        PrimaryLanguage primaryLanguage,
        RepositoryTopics repositoryTopics
) {}