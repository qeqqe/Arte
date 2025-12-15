package com.arte.ingestion.dto.github;


import java.util.List;

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
            List<RepositoryNode> nodes
    ) {}
}