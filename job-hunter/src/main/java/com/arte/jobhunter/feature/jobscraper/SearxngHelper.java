package com.arte.jobhunter.feature.jobscraper;

import com.arte.jobhunter.dto.SearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
public class SearxngHelper {
    @Value("${searxng.instance.url}")
    private String instanceUrl;
    private HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SearxngHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public SearchResponse search(String query) {
        if (instanceUrl == null || instanceUrl.isBlank()) {
            throw new IllegalStateException("Searxng instance URL is not configured");
        }
        if (query == null) {
            query = "";
        }

        try {
            if (httpClient == null) {
                httpClient = HttpClient.newBuilder().build();
            }

            URI uri = UriComponentsBuilder.fromUriString(instanceUrl)
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .build()
                    .toUri();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .header("Accept", "application/json")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status != 200) {
                throw new RuntimeException("Failed to fetch search results: HTTP " + status + " - " + response.body());
            }

            return objectMapper.readValue(response.body(), SearchResponse.class);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Searxng search interrupted", ie);
            throw new RuntimeException("Searxng search interrupted", ie);
        } catch (Exception e) {
            log.error("Error during Searxng search request", e);
            throw new RuntimeException("Error during Searxng search request", e);
        }
    }


}
