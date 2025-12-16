package com.arte.ingestion.controller;

import com.arte.ingestion.service.GitHubIngestionService;
import com.arte.ingestion.service.LeetCodeIngestionService;
import com.arte.ingestion.service.ResumeProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestionController.class)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GitHubIngestionService gitHubIngestionService;
    @MockitoBean
    private LeetCodeIngestionService leetCodeIngestionService;
    @MockitoBean
    private ResumeProcessingService resumeProcessingService;

    @Test
    void ingestGitHub_success_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        var result = new GitHubIngestionService.IngestionResult(
                true, "Successfully ingested", 3, List.of("repo1", "repo2", "repo3")
        );
        
        when(gitHubIngestionService.ingestGitHubData(userId)).thenReturn(result);

        mockMvc.perform(post("/api/ingestion/github/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reposProcessed").value(3));
    }

    @Test
    void ingestGitHub_userNotFound_returns400() throws Exception {
        UUID userId = UUID.randomUUID();
        
        when(gitHubIngestionService.ingestGitHubData(userId))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(post("/api/ingestion/github/" + userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void ingestGitHub_noGitHubData_returns400() throws Exception {
        UUID userId = UUID.randomUUID();
        var result = new GitHubIngestionService.IngestionResult(
                false, "No GitHub data found", 0, List.of()
        );
        
        when(gitHubIngestionService.ingestGitHubData(userId)).thenReturn(result);

        mockMvc.perform(post("/api/ingestion/github/" + userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void ingestLeetCode_success_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        var result = new LeetCodeIngestionService.IngestionResult(true, "Success", 150);
        
        when(leetCodeIngestionService.ingestLeetCodeData(any(UUID.class), anyString()))
                .thenReturn(result);

        mockMvc.perform(post("/api/ingestion/leetcode/" + userId)
                        .param("leetcodeUsername", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.problemsSolved").value(150));
    }

    @Test
    void ingestLeetCode_missingUsername_returns400() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/ingestion/leetcode/" + userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processResume_success_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        var result = new ResumeProcessingService.ProcessingResult(true, "Success", 500);
        
        when(resumeProcessingService.processResume(any(UUID.class), any()))
                .thenReturn(result);

        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/ingestion/resume/" + userId)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.wordCount").value(500));
    }

    @Test
    void processResume_invalidFile_returns400() throws Exception {
        UUID userId = UUID.randomUUID();
        var result = new ResumeProcessingService.ProcessingResult(
                false, "Invalid file type", 0
        );
        
        when(resumeProcessingService.processResume(any(UUID.class), any()))
                .thenReturn(result);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "text content".getBytes()
        );

        mockMvc.perform(multipart("/api/ingestion/resume/" + userId)
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void fullIngestion_githubOnly_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        var githubResult = new GitHubIngestionService.IngestionResult(
                true, "GitHub success", 2, List.of("repo1", "repo2")
        );
        
        when(gitHubIngestionService.ingestGitHubData(userId)).thenReturn(githubResult);

        mockMvc.perform(post("/api/ingestion/full/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void fullIngestion_withLeetCode_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        var githubResult = new GitHubIngestionService.IngestionResult(
                true, "GitHub success", 2, List.of("repo1", "repo2")
        );
        var leetcodeResult = new LeetCodeIngestionService.IngestionResult(true, "LeetCode success", 100);
        
        when(gitHubIngestionService.ingestGitHubData(userId)).thenReturn(githubResult);
        when(leetCodeIngestionService.ingestLeetCodeData(any(UUID.class), anyString()))
                .thenReturn(leetcodeResult);

        mockMvc.perform(post("/api/ingestion/full/" + userId)
                        .param("leetcodeUsername", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
