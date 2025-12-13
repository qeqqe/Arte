package com.arte.ingestion.entity.github;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class GitHubStats {
    private Integer totalStars;
    private Integer totalForks;
    private Integer totalPinnedRepos;
    private List<RepoSummary> pinnedRepos;
    private Map<String, Integer> languageDistribution;
    private List<String> topTopics;
    private LocalDateTime lastSynced;
}