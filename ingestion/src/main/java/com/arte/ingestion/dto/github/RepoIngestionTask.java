package com.arte.ingestion.dto.github;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RepoIngestionTask {
    private UUID userId;
    private String repoName;
    private String repoUrl;
    private String owner;
    private boolean fetchReadme;
}