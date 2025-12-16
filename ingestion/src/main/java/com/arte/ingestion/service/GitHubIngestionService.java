package com.arte.ingestion.service;

import com.arte.ingestion.client.GitHubGraphQLClient;
import com.arte.ingestion.dto.github.GitHubGraphQLResponse;
import com.arte.ingestion.dto.github.RepositoryNode;
import com.arte.ingestion.entity.UserInfo;
import com.arte.ingestion.entity.UserKnowledgeBase;
import com.arte.ingestion.entity.Users;
import com.arte.ingestion.entity.github.GitHubStats;
import com.arte.ingestion.entity.github.RepoSummary;
import com.arte.ingestion.repository.UserInfoRepository;
import com.arte.ingestion.repository.UserKnowledgeBaseRepository;
import com.arte.ingestion.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubIngestionService {

    private static final String SOURCE_TYPE = "github";

    private final GitHubGraphQLClient gitHubGraphQLClient;
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserKnowledgeBaseRepository knowledgeBaseRepository;
    private final ObjectMapper objectMapper;

    /**
     * Ingests user's github pinned repos, readme, and after that this triggers embedding gen.
     *
     * @param userId user's UUID? what else lol
     * @return IngestionResult with all the details of what was ingested
     */
    @Transactional
    public IngestionResult ingestGitHubData(UUID userId) {
        log.info("Starting GitHub ingestion for user: {}", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        String githubUsername = user.getGithubUsername();
        String githubToken = user.getGithubToken();

        // 1. fetch the pinned repos
        GitHubGraphQLResponse response = gitHubGraphQLClient.fetchPinnedRepos(githubUsername, githubToken);

        if (response == null || response.data() == null || response.data().user() == null) {
            log.warn("No GitHub data found for user: {}", githubUsername);
            return new IngestionResult(false, "No GitHub data found", 0, List.of());
        }

        List<RepositoryNode> pinnedRepos = response.data().user().pinnedItems().nodes();
        log.info("Found {} pinned repos for user: {}", pinnedRepos.size(), githubUsername);

        // 2. build the stats and fetch readme.md
        List<RepoSummary> repoSummaries = new ArrayList<>();
        List<UserKnowledgeBase> knowledgeBaseEntries = new ArrayList<>();
        Map<String, Integer> languageDistribution = new HashMap<>();
        Set<String> allTopics = new HashSet<>();
        int totalStars = 0;
        int totalForks = 0;

        for (RepositoryNode repo : pinnedRepos) {
            // aggregate up all the stats
            totalStars += repo.stargazerCount() != null ? repo.stargazerCount() : 0;
            totalForks += repo.forkCount() != null ? repo.forkCount() : 0;

            String primaryLang = repo.primaryLanguage() != null ? repo.primaryLanguage().name() : "Unknown";
            languageDistribution.merge(primaryLang, 1, Integer::sum);

            if (repo.repositoryTopics() != null && repo.repositoryTopics().nodes() != null) {
                repo.repositoryTopics().nodes().forEach(t -> allTopics.add(t.topic().name()));
            }

            // build repo summary
            repoSummaries.add(RepoSummary.builder()
                    .name(repo.name())
                    .url(repo.url())
                    .stars(repo.stargazerCount())
                    .forks(repo.forkCount())
                    .primaryLanguage(primaryLang)
                    .build());

            // fetch the readme.md and build knowledge base entry
            String readme = gitHubGraphQLClient.fetchReadme(repo.url(), githubToken);
            String content = buildRepoContent(repo, readme);

            List<String> topics = extractTopics(repo);
            Map<String, Object> metadata = Map.of(
                    "repoName", repo.name(),
                    "repoUrl", repo.url(),
                    "primaryLanguage", primaryLang,
                    "stars", repo.stargazerCount() != null ? repo.stargazerCount() : 0,
                    "forks", repo.forkCount() != null ? repo.forkCount() : 0,
                    "topics", topics
            );

            UserKnowledgeBase entry = knowledgeBaseRepository
                    .findByUserIdAndSourceTypeAndSourceUrl(userId, SOURCE_TYPE, repo.url())
                    .map(existing -> {
                        existing.setContent(content);
                        existing.setMetadata(metadata);
                        return existing;
                    })
                    .orElse(UserKnowledgeBase.builder()
                            .user(user)
                            .content(content)
                            .sourceType(SOURCE_TYPE)
                            .sourceUrl(repo.url())
                            .metadata(metadata)
                            .build());

            knowledgeBaseEntries.add(knowledgeBaseRepository.save(entry));
        }

        // 3. update the user_info with github stats
        GitHubStats githubStats = GitHubStats.builder()
                .totalStars(totalStars)
                .totalForks(totalForks)
                .totalPinnedRepos(pinnedRepos.size())
                .pinnedRepos(repoSummaries)
                .languageDistribution(languageDistribution)
                .topTopics(new ArrayList<>(allTopics))
                .lastSynced(LocalDateTime.now())
                .build();

        UserInfo userInfo = userInfoRepository.findById(userId)
                .orElse(UserInfo.builder()
                        .user(user)
                        .build());

        userInfo.setGithubStats(objectMapper.convertValue(githubStats, Map.class));
        userInfo.setLastIngestedAt(Instant.now());
        userInfoRepository.save(userInfo);

        log.info("GitHub ingestion completed for user {}: {} repos processed", userId, pinnedRepos.size());

        return new IngestionResult(
                true,
                "Successfully ingested GitHub data",
                pinnedRepos.size(),
                repoSummaries.stream().map(RepoSummary::getName).toList()
        );
    }

    private String buildRepoContent(RepositoryNode repo, String readme) {
        StringBuilder content = new StringBuilder();
        content.append("Repository: ").append(repo.name()).append("\n");
        content.append("URL: ").append(repo.url()).append("\n");

        if (repo.description() != null && !repo.description().isBlank()) {
            content.append("Description: ").append(repo.description()).append("\n");
        }

        if (repo.primaryLanguage() != null) {
            content.append("Primary Language: ").append(repo.primaryLanguage().name()).append("\n");
        }

        List<String> topics = extractTopics(repo);
        if (!topics.isEmpty()) {
            content.append("Topics: ").append(String.join(", ", topics)).append("\n");
        }

        content.append("Stars: ").append(repo.stargazerCount()).append("\n");
        content.append("Forks: ").append(repo.forkCount()).append("\n");

        if (readme != null && !readme.isBlank()) {
            content.append("\n--- README ---\n").append(readme);
        }

        return content.toString();
    }

    private List<String> extractTopics(RepositoryNode repo) {
        if (repo.repositoryTopics() == null || repo.repositoryTopics().nodes() == null) {
            return List.of();
        }
        return repo.repositoryTopics().nodes().stream()
                .map(t -> t.topic().name())
                .toList();
    }

    public record IngestionResult(
            boolean success,
            String message,
            int reposProcessed,
            List<String> repoNames
    ) {}
}
