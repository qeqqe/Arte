package com.arte.ingestion.dto.grpc;

import java.util.List;
import java.util.UUID;

public record GitHubProcessingRequest(
        UUID userId,
        List<RepoData> pinnedRepos
) {}