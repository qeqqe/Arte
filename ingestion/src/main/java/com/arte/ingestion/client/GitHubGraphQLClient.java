package com.arte.ingestion.client;


import com.arte.ingestion.dto.github.GitHubGraphQLResponse;
import com.arte.ingestion.dto.github.GitHubReadmeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@Slf4j
public class GitHubGraphQLClient {

    private static final String GITHUB_GRAPHQL_URL = "https://api.github.com/graphql";
    private static final String GITHUB_REST_URL = "https://api.github.com";

    public GitHubGraphQLResponse fetchPinnedRepos(String username, String token) {
        String query = """
            {
                user(login: "%s") {
                    pinnedItems(first: 6, types: REPOSITORY) {
                        nodes {
                            ... on Repository {
                                name
                                description
                                url
                                stargazerCount
                                forkCount
                                primaryLanguage {
                                    name
                                    color
                                }
                                repositoryTopics(first: 10) {
                                    nodes {
                                        topic {
                                            name
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.formatted(username);

        Map<String, String> requestBody = Map.of("query", query);

        WebClient graphqlClient = WebClient.create(GITHUB_GRAPHQL_URL);
        
        return graphqlClient.post()
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GitHubGraphQLResponse.class)
                .doOnError(e -> log.error("Failed to fetch pinned repos for user: {}", username, e))
                .block();
    }

    public String fetchReadme(String repoUrl, String token) {
        String[] parts = repoUrl.replace("https://github.com/", "").split("/");
        String owner = parts[0];
        String repo = parts[1];

        String readmeUrl = String.format("%s/repos/%s/%s/readme", GITHUB_REST_URL, owner, repo);

        WebClient restClient = WebClient.create();

        return restClient.get()
                .uri(readmeUrl)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(GitHubReadmeResponse.class)
                .map(response -> {
                    byte[] decodedBytes = Base64.getMimeDecoder().decode(response.content());
                    return new String(decodedBytes, StandardCharsets.UTF_8);
                })
                .onErrorResume(e -> {
                    log.warn("Failed to fetch README: {}", e.getMessage());
                    return Mono.just("");
                })
                .block();
    }
}
