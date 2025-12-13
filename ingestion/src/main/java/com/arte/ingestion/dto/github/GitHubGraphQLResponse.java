package com.arte.ingestion.dto.github;


public record GitHubGraphQLResponse(
        DataWrapper data
) {
    public record DataWrapper(
            UserWrapper user
    ) {}

    public record UserWrapper(
            PinnedItems pinnedItems
    ) {}

    public record PinnedItems(
            java.util.List<RepositoryNode> nodes
    ) {}
}