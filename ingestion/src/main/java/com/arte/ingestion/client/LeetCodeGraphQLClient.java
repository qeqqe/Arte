/**
 * Core implementation from https://github.com/RealPeha/leetcode-graphql
 * Credit to the original authors
 */
package com.arte.ingestion.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeetCodeGraphQLClient {

    private static final String LEETCODE_GRAPHQL_URL = "https://leetcode.com/graphql";

    private final WebClient webClient = WebClient.builder()
            .baseUrl(LEETCODE_GRAPHQL_URL)
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Referer", "https://leetcode.com")
            .build();

    public JsonNode fetchUserProfile(String username) {
        String query = """
            query getUserProfile($username: String!) {
                matchedUser(username: $username) {
                    username
                    profile {
                        realName
                        aboutMe
                        ranking
                        reputation
                        starRating
                    }
                    submitStatsGlobal {
                        acSubmissionNum {
                            difficulty
                            count
                        }
                    }
                    badges {
                        name
                        icon
                    }
                    activeBadge {
                        name
                        icon
                    }
                }
            }
            """;

        Map<String, Object> requestBody = Map.of(
                "query", query,
                "variables", Map.of("username", username)
        );

        try {
            return webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .doOnError(e -> log.error("Failed to fetch LeetCode profile for: {}", username, e))
                    .block();
        } catch (Exception e) {
            log.error("LeetCode API call failed for user: {}", username, e);
            return null;
        }
    }

    // recent submissions
    public JsonNode fetchRecentSubmissions(String username, int limit) {
        String query = """
            query getRecentSubmissions($username: String!, $limit: Int!) {
                recentAcSubmissionList(username: $username, limit: $limit) {
                    id
                    title
                    titleSlug
                    timestamp
                    lang
                }
            }
            """;

        Map<String, Object> requestBody = Map.of(
                "query", query,
                "variables", Map.of("username", username, "limit", limit)
        );

        try {
            return webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .doOnError(e -> log.error("Failed to fetch LeetCode submissions for: {}", username, e))
                    .block();
        } catch (Exception e) {
            log.error("LeetCode submissions API call failed for user: {}", username, e);
            return null;
        }
    }

    // contest ranking
    public JsonNode fetchContestRanking(String username) {
        String query = """
            query getUserContestRanking($username: String!) {
                userContestRanking(username: $username) {
                    attendedContestsCount
                    rating
                    globalRanking
                    topPercentage
                }
                userContestRankingHistory(username: $username) {
                    attended
                    rating
                    ranking
                    contest {
                        title
                        startTime
                    }
                }
            }
            """;

        Map<String, Object> requestBody = Map.of(
                "query", query,
                "variables", Map.of("username", username)
        );

        try {
            return webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .doOnError(e -> log.error("Failed to fetch LeetCode contest ranking for: {}", username, e))
                    .block();
        } catch (Exception e) {
            log.error("LeetCode contest API call failed for user: {}", username, e);
            return null;
        }
    }

    // language stats
    public JsonNode fetchLanguageStats(String username) {
        String query = """
            query languageStats($username: String!) {
                matchedUser(username: $username) {
                    languageProblemCount {
                        languageName
                        problemsSolved
                    }
                }
            }
            """;

        Map<String, Object> requestBody = Map.of(
                "query", query,
                "variables", Map.of("username", username)
        );

        try {
            return webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .doOnError(e -> log.error("Failed to fetch LeetCode language stats for: {}", username, e))
                    .block();
        } catch (Exception e) {
            log.error("LeetCode language stats API call failed for user: {}", username, e);
            return null;
        }
    }
}
