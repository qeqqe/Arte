package com.arte.ingestion.service;

import com.arte.ingestion.client.LeetCodeGraphQLClient;
import com.arte.ingestion.dto.leetcode.LeetCodeStats;
import com.arte.ingestion.entity.UserInfo;
import com.arte.ingestion.entity.UserKnowledgeBase;
import com.arte.ingestion.entity.Users;
import com.arte.ingestion.repository.UserInfoRepository;
import com.arte.ingestion.repository.UserKnowledgeBaseRepository;
import com.arte.ingestion.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service 
@RequiredArgsConstructor
@Slf4j
public class LeetCodeIngestionService {

    private static final String SOURCE_TYPE = "leetcode";
    private static final int RECENT_SUBMISSIONS_LIMIT = 20;

    private final LeetCodeGraphQLClient leetCodeClient;
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserKnowledgeBaseRepository knowledgeBaseRepository;
    private final ObjectMapper objectMapper;

    /**
     * Ingests leetcode data for a user's profile, submissions, contest ranking and triggers embedding generation through gRPC
     *
     * @param userId           user's UUID
     * @param leetcodeUsername user's LeetCode username
     * @return IngestionResult with details of what was ingested
     */
    @Transactional
    public IngestionResult ingestLeetCodeData(UUID userId, String leetcodeUsername) {
        log.info("Starting LeetCode ingestion for user: {} (leetcode: {})", userId, leetcodeUsername);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 1. fetch all the LeetCode data
        JsonNode profileData = leetCodeClient.fetchUserProfile(leetcodeUsername);
        JsonNode submissionsData = leetCodeClient.fetchRecentSubmissions(leetcodeUsername, RECENT_SUBMISSIONS_LIMIT);
        JsonNode contestData = leetCodeClient.fetchContestRanking(leetcodeUsername);
        JsonNode languageData = leetCodeClient.fetchLanguageStats(leetcodeUsername);

        if (profileData == null || !profileData.has("data") || profileData.get("data").get("matchedUser").isNull()) {
            log.warn("No LeetCode profile found for user: {}", leetcodeUsername);
            return new IngestionResult(false, "LeetCode user not found: " + leetcodeUsername, 0);
        }

        // 2. parse and build stats
        LeetCodeStats stats = buildLeetCodeStats(profileData, submissionsData, contestData, languageData);

        // 3. update user_info with the stats
        UserInfo userInfo = userInfoRepository.findById(userId)
                .orElse(UserInfo.builder()
                        .user(user)
                        .build());

        userInfo.setLeetcodeStats(objectMapper.convertValue(stats, Map.class));
        userInfo.setLastIngestedAt(Instant.now());
        userInfoRepository.save(userInfo);

        // 4. create knowledge base entry for leetcode profile
        String content = buildLeetCodeContent(stats);
        Map<String, Object> metadata = Map.of(
                "username", leetcodeUsername,
                "totalSolved", stats.getTotalSolved() != null ? stats.getTotalSolved() : 0,
                "contestRating", stats.getContestRating() != null ? stats.getContestRating() : 0,
                "ranking", stats.getRanking() != null ? stats.getRanking() : 0
        );

        String sourceUrl = "https://leetcode.com/u/" + leetcodeUsername;
        UserKnowledgeBase entry = knowledgeBaseRepository
                .findByUserIdAndSourceTypeAndSourceUrl(userId, SOURCE_TYPE, sourceUrl)
                .map(existing -> {
                    existing.setContent(content);
                    existing.setMetadata(metadata);
                    return existing;
                })
                .orElse(UserKnowledgeBase.builder()
                        .user(user)
                        .content(content)
                        .sourceType(SOURCE_TYPE)
                        .sourceUrl(sourceUrl)
                        .metadata(metadata)
                        .build());

        knowledgeBaseRepository.save(entry);

        log.info("LeetCode ingestion completed for user {}", userId);

        return new IngestionResult(
                true,
                "Successfully ingested LeetCode data",
                stats.getTotalSolved() != null ? stats.getTotalSolved() : 0
        );
    }

    private LeetCodeStats buildLeetCodeStats(JsonNode profile, JsonNode submissions, JsonNode contest, JsonNode language) {
        LeetCodeStats.LeetCodeStatsBuilder builder = LeetCodeStats.builder();

        // parse profile data
        JsonNode matchedUser = profile.path("data").path("matchedUser");
        builder.username(matchedUser.path("username").asText());

        JsonNode profileNode = matchedUser.path("profile");
        builder.ranking(profileNode.path("ranking").asInt(0));
        builder.reputation(profileNode.path("reputation").asInt(0));
        builder.starRating(profileNode.path("starRating").asDouble(0));
        builder.aboutMe(profileNode.path("aboutMe").asText(""));

        // parse problem stats
        JsonNode submitStats = matchedUser.path("submitStatsGlobal").path("acSubmissionNum");
        if (submitStats.isArray()) {
            int total = 0, easy = 0, medium = 0, hard = 0;
            for (JsonNode stat : submitStats) {
                String difficulty = stat.path("difficulty").asText();
                int count = stat.path("count").asInt(0);
                switch (difficulty) {
                    case "All" -> total = count;
                    case "Easy" -> easy = count;
                    case "Medium" -> medium = count;
                    case "Hard" -> hard = count;
                }
            }
            builder.totalSolved(total);
            builder.easySolved(easy);
            builder.mediumSolved(medium);
            builder.hardSolved(hard);
        }

        // parse badges
        JsonNode badges = matchedUser.path("badges");
        if (badges.isArray()) {
            List<String> badgeNames = new ArrayList<>();
            for (JsonNode badge : badges) {
                badgeNames.add(badge.path("name").asText());
            }
            builder.badges(badgeNames);
        }

        JsonNode activeBadge = matchedUser.path("activeBadge");
        if (!activeBadge.isNull() && activeBadge.has("name")) {
            builder.activeBadge(activeBadge.path("name").asText());
        }

        // parse contest data
        if (contest != null && contest.has("data")) {
            JsonNode contestRanking = contest.path("data").path("userContestRanking");
            if (!contestRanking.isNull()) {
                builder.contestsAttended(contestRanking.path("attendedContestsCount").asInt(0));
                builder.contestRating(contestRanking.path("rating").asDouble(0));
                builder.globalRanking(contestRanking.path("globalRanking").asInt(0));
                builder.topPercentage(contestRanking.path("topPercentage").asDouble(0));
            }
        }

        // parse language stats
        if (language != null && language.has("data")) {
            JsonNode langStats = language.path("data").path("matchedUser").path("languageProblemCount");
            if (langStats.isArray()) {
                Map<String, Integer> languageMap = new HashMap<>();
                for (JsonNode lang : langStats) {
                    languageMap.put(
                            lang.path("languageName").asText(),
                            lang.path("problemsSolved").asInt(0)
                    );
                }
                builder.languageStats(languageMap);
            }
        }

        // parse recent submissions
        if (submissions != null && submissions.has("data")) {
            JsonNode submissionList = submissions.path("data").path("recentAcSubmissionList");
            if (submissionList.isArray()) {
                List<LeetCodeStats.RecentSubmission> recentSubs = new ArrayList<>();
                for (JsonNode sub : submissionList) {
                    recentSubs.add(LeetCodeStats.RecentSubmission.builder()
                            .title(sub.path("title").asText())
                            .titleSlug(sub.path("titleSlug").asText())
                            .language(sub.path("lang").asText())
                            .timestamp(sub.path("timestamp").asLong())
                            .build());
                }
                builder.recentSubmissions(recentSubs);
            }
        }

        return builder.build();
    }

    private String buildLeetCodeContent(LeetCodeStats stats) {
        StringBuilder content = new StringBuilder();
        content.append("LeetCode Profile: ").append(stats.getUsername()).append("\n\n");

        content.append("=== Problem Statistics ===\n");
        content.append("Total Problems Solved: ").append(stats.getTotalSolved()).append("\n");
        content.append("Easy: ").append(stats.getEasySolved()).append("\n");
        content.append("Medium: ").append(stats.getMediumSolved()).append("\n");
        content.append("Hard: ").append(stats.getHardSolved()).append("\n\n");

        if (stats.getContestRating() != null && stats.getContestRating() > 0) {
            content.append("=== Contest Statistics ===\n");
            content.append("Contest Rating: ").append(String.format("%.0f", stats.getContestRating())).append("\n");
            content.append("Contests Attended: ").append(stats.getContestsAttended()).append("\n");
            content.append("Global Ranking: ").append(stats.getGlobalRanking()).append("\n");
            content.append("Top Percentage: ").append(String.format("%.2f%%", stats.getTopPercentage())).append("\n\n");
        }

        if (stats.getLanguageStats() != null && !stats.getLanguageStats().isEmpty()) {
            content.append("=== Programming Languages ===\n");
            stats.getLanguageStats().entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(5)
                    .forEach(e -> content.append(e.getKey()).append(": ").append(e.getValue()).append(" problems\n"));
            content.append("\n");
        }

        if (stats.getBadges() != null && !stats.getBadges().isEmpty()) {
            content.append("=== Badges ===\n");
            content.append(String.join(", ", stats.getBadges())).append("\n\n");
        }

        if (stats.getRecentSubmissions() != null && !stats.getRecentSubmissions().isEmpty()) {
            content.append("=== Recent Submissions ===\n");
            stats.getRecentSubmissions().stream()
                    .limit(10)
                    .forEach(sub -> content.append("- ").append(sub.getTitle())
                            .append(" (").append(sub.getLanguage()).append(")\n"));
        }

        return content.toString();
    }

    public record IngestionResult(
            boolean success,
            String message,
            int problemsSolved
    ) {}
}
