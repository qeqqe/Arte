package com.arte.processing.dto.leetcode;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Jacksonized
public class LeetCodeStats {
    private String username;
    private Integer ranking;
    private Integer reputation;
    private Double starRating;
    private String aboutMe;
    
    // problem stats
    private Integer totalSolved;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;
    
    // contest stats
    private Integer contestsAttended;
    private Double contestRating;
    private Integer globalRanking;
    private Double topPercentage;
    
    // language distribution
    private Map<String, Integer> languageStats;
    
    // badges
    private List<String> badges;
    private String activeBadge;

    private List<RecentSubmission> recentSubmissions;
    
    @Data
    @Builder
    @Jacksonized
    public static class RecentSubmission {
        private String title;
        private String titleSlug;
        private String language;
        private Long timestamp;
    }
}
