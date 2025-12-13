package com.arte.ingestion.model.github;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubStatsData implements Serializable {
    private Integer totalStars;
    private Integer totalForks;
    private Integer totalPinnedRepos;
    private List<PinnedRepoSummary> pinnedRepos;
    private Map<String, LanguageStats> languageBreakdown;
    private List<String> allTopics;
    private LocalDateTime lastSynced;
}