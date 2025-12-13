package com.arte.ingestion.model.github;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinnedRepoSummary implements Serializable {
    private String name;
    private String url;
    private String description;
    private Integer stars;
    private Integer forks;
    private String primaryLanguage;
    private String languageColor;
    private List<String> topics;
}