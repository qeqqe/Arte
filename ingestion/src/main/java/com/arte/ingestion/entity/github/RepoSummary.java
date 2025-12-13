package com.arte.ingestion.entity.github;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RepoSummary {
    private String name;
    private String url;
    private Integer stars;
    private Integer forks;
    private String primaryLanguage;
}