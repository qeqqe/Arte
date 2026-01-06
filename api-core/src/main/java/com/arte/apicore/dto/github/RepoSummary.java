package com.arte.apicore.dto.github;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class RepoSummary {
    private String name;
    private String url;
    private Integer stars;
    private Integer forks;
    private String primaryLanguage;
}