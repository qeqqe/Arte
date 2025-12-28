package com.arte.ingestion.controller;

import com.arte.ingestion.entity.UserInfo;
import com.arte.ingestion.repository.UserInfoRepository;
import com.arte.ingestion.service.GitHubIngestionService;
import com.arte.ingestion.service.LeetCodeIngestionService;
import com.arte.ingestion.service.ResumeProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
@Slf4j
public class IngestionController {

    private final GitHubIngestionService gitHubIngestionService;
    private final LeetCodeIngestionService leetCodeIngestionService;
    private final ResumeProcessingService resumeProcessingService;
    private final UserInfoRepository userInfoRepository;

    /**
     *  GitHub data ingestion for a user.
     * Fetches pinned repos, READMEs, and generates embeddings.
     */
    @PostMapping("/github/{userId}")
    public ResponseEntity<IngestionResponse> ingestGitHub(@PathVariable UUID userId) {
        log.info("Received GitHub ingestion request for user: {}", userId);
        
        try {
            var result = gitHubIngestionService.ingestGitHubData(userId);
            
            if (result.success()) {
                return ResponseEntity.ok(new IngestionResponse(
                        true,
                        result.message(),
                        Map.of(
                                "reposProcessed", result.reposProcessed(),
                                "repos", result.repoNames()
                        )
                ));
            } else {
                return ResponseEntity.badRequest().body(new IngestionResponse(
                        false,
                        result.message(),
                        Map.of()
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new IngestionResponse(
                    false,
                    e.getMessage(),
                    Map.of()
            ));
        } catch (Exception e) {
            log.error("GitHub ingestion failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().body(new IngestionResponse(
                    false,
                    "Internal error: " + e.getMessage(),
                    Map.of()
            ));
        }
    }

    /**
     * Triggers LeetCode data ingestion for a user.
     * Fetches profile, submissions, contest ranking, and generates embeddings.
     */
    @PostMapping("/leetcode/{userId}")
    public ResponseEntity<IngestionResponse> ingestLeetCode(
            @PathVariable UUID userId,
            @RequestParam String leetcodeUsername) {
        log.info("Received LeetCode ingestion request for user: {} (leetcode: {})", userId, leetcodeUsername);
        
        try {
            var result = leetCodeIngestionService.ingestLeetCodeData(userId, leetcodeUsername);
            
            if (result.success()) {
                return ResponseEntity.ok(new IngestionResponse(
                        true,
                        result.message(),
                        Map.of("problemsSolved", result.problemsSolved())
                ));
            } else {
                return ResponseEntity.badRequest().body(new IngestionResponse(
                        false,
                        result.message(),
                        Map.of()
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new IngestionResponse(
                    false,
                    e.getMessage(),
                    Map.of()
            ));
        } catch (Exception e) {
            log.error("LeetCode ingestion failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().body(new IngestionResponse(
                    false,
                    "Internal error: " + e.getMessage(),
                    Map.of()
            ));
        }
    }

    /**
     * Processes a resume PDF for a user.
     * Extracts text, stores summary, and generates embeddings.
     */
    @PostMapping(value = "/resume/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngestionResponse> processResume(
            @PathVariable UUID userId,
            @RequestParam("file") MultipartFile file) {
        log.info("Received resume processing request for user: {}, file: {}", userId, file.getOriginalFilename());
        
        try {
            var result = resumeProcessingService.processResume(userId, file);
            
            if (result.success()) {
                return ResponseEntity.ok(new IngestionResponse(
                        true,
                        result.message(),
                        Map.of(
                                "fileName", file.getOriginalFilename(),
                                "wordCount", result.wordCount()
                        )
                ));
            } else {
                return ResponseEntity.badRequest().body(new IngestionResponse(
                        false,
                        result.message(),
                        Map.of()
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new IngestionResponse(
                    false,
                    e.getMessage(),
                    Map.of()
            ));
        } catch (IOException e) {
            log.error("Resume processing failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().body(new IngestionResponse(
                    false,
                    "Failed to process PDF: " + e.getMessage(),
                    Map.of()
            ));
        } catch (Exception e) {
            log.error("Resume processing failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().body(new IngestionResponse(
                    false,
                    "Internal error: " + e.getMessage(),
                    Map.of()
            ));
        }
    }

    /**
     * Triggers full ingestion for a user (GitHub + LeetCode if username provided).
     */
    @PostMapping("/full/{userId}")
    public ResponseEntity<IngestionResponse> fullIngestion(
            @PathVariable UUID userId,
            @RequestParam(required = false) String leetcodeUsername) {
        log.info("Received full ingestion request for user: {}", userId);
        
        StringBuilder messages = new StringBuilder();
        boolean allSuccess = true;
        
        // GitHub ingestion
        try {
            var githubResult = gitHubIngestionService.ingestGitHubData(userId);
            messages.append("GitHub: ").append(githubResult.message());
            if (!githubResult.success()) {
                allSuccess = false;
            }
        } catch (Exception e) {
            messages.append("GitHub: Failed - ").append(e.getMessage());
            allSuccess = false;
        }
        
        // LeetCode ingestion (if username provided)
        if (leetcodeUsername != null && !leetcodeUsername.isBlank()) {
            messages.append(" | ");
            try {
                var leetcodeResult = leetCodeIngestionService.ingestLeetCodeData(userId, leetcodeUsername);
                messages.append("LeetCode: ").append(leetcodeResult.message());
                if (!leetcodeResult.success()) {
                    allSuccess = false;
                }
            } catch (Exception e) {
                messages.append("LeetCode: Failed - ").append(e.getMessage());
                allSuccess = false;
            }
        }
        
        return ResponseEntity.ok(new IngestionResponse(
                allSuccess,
                messages.toString(),
                Map.of()
        ));
    }

    /**
     * Test endpoint to retrieve user info and verify deserialization
     */
    @GetMapping("/test-userinfo/{userId}")
    public ResponseEntity<IngestionResponse> testGetUserInfo(@PathVariable UUID userId) {
        log.info("Testing getUserInfo deserialization for user: {}", userId);
        
        try {
            UserInfo userInfo = userInfoRepository.findById(userId).orElse(null);
            
            if (userInfo == null) {
                return ResponseEntity.ok(new IngestionResponse(
                        false,
                        "User info not found",
                        Map.of()
                ));
            }
            
            // This will test deserialization
            var githubStats = userInfo.getGithubStats();
            var leetcodeStats = userInfo.getLeetcodeStats();
            var resumeSummary = userInfo.getResumeSummary();
            
            return ResponseEntity.ok(new IngestionResponse(
                    true,
                    "User info retrieved and deserialized successfully",
                    Map.of(
                            "hasGithub", githubStats != null,
                            "hasLeetcode", leetcodeStats != null,
                            "hasResume", resumeSummary != null,
                            "githubRepos", githubStats != null ? githubStats.getTotalPinnedRepos() : 0,
                            "leetcodeSolved", leetcodeStats != null ? leetcodeStats.getTotalSolved() : 0
                    )
            ));
        } catch (Exception e) {
            log.error("Error testing getUserInfo deserialization", e);
            return ResponseEntity.ok(new IngestionResponse(
                    false,
                    "Error: " + e.getMessage(),
                    Map.of("errorType", e.getClass().getSimpleName())
            ));
        }
    }

    public record IngestionResponse(
            boolean success,
            String message,
            Map<String, Object> data
    ) {}
}
