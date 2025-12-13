package com.arte.ingestion.dto.grpc;

import java.util.List;

public record RepoData(
        String name,
        String url,
        String description,
        String primaryLanguage,
        List<String> topics,
        String readmeContent
) {}