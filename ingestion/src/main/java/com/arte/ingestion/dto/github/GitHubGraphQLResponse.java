package com.arte.ingestion.dto.github;

import java.util.List;

public record GitHubGraphQLResponse(GitHubData data) {
    public record GitHubData(GitHubUser user) {}
    public record GitHubUser(PinnedItems pinnedItems) {}
    public record PinnedItems(List<RepositoryNode> nodes) {}

    public record RepositoryNode(
            String name,
            String description,
            String url,
            Integer stargazerCount,
            Integer forkCount,
            PrimaryLanguage primaryLanguage,
            RepositoryTopics repositoryTopics
    ) {}

    public record PrimaryLanguage(String name, String color) {}
    public record RepositoryTopics(List<TopicNode> nodes) {}
    public record TopicNode(Topic topic) {}
    public record Topic(String name) {}
}