package com.arte.apicore.controller;

import com.arte.apicore.client.IngestionServiceGrpcClient;
import com.arte.apicore.dto.proto.*;
import com.arte.apicore.grpc.*;
import com.arte.apicore.service.auth.strategy.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {
    public final IngestionServiceGrpcClient ingestionServiceGrpcClient;

    public IngestionController(IngestionServiceGrpcClient ingestionServiceGrpcClient) {
        this.ingestionServiceGrpcClient = ingestionServiceGrpcClient;
    }

    @PostMapping("/leetcode")
    public ResponseEntity<LeetCodeResponseDTO> ingestLeetcode(
            @RequestBody LeetCodeRequestDTO request, @AuthenticationPrincipal UserPrincipal user
    ) {
        IngestLeetCodeResponse response = ingestionServiceGrpcClient
                .ingestLeetCode(UUID.fromString(user.userId()), request.leetcodeUsername());

        return ResponseEntity.ok(
                new LeetCodeResponseDTO(response.getSuccess(), response.getMessage(), response.getProblemsSolved())
        );
    }


    @GetMapping("/github")
    public ResponseEntity<GitHubResponseDTO> ingestGithub(
            @AuthenticationPrincipal UserPrincipal user) {
        IngestGitHubResponse response = ingestionServiceGrpcClient.ingestGitHub(UUID.fromString(user.userId()));
        return ResponseEntity.ok(
                new GitHubResponseDTO(response.getSuccess(), response.getMessage(), response.getReposProcessed(), response.getRepoNamesList())
        );
    }

    @PostMapping(path = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponseDTO> ingestResume(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal user
    ) throws IOException {
        IngestResumeResponse response = ingestionServiceGrpcClient.ingestResume(
                UUID.fromString(user.userId()),
                file.getOriginalFilename(),
                file.getBytes()
        );
        return ResponseEntity.ok(
                new ResumeResponseDTO(response.getSuccess(), response.getMessage(), response.getWordCount())
        );
    }

    @PostMapping("/linkedin")
    public ResponseEntity<LinkedInJobResponseDTO> ingestLinkedInJob
            (@RequestBody LinkedInJobRequestDTO request,
             @AuthenticationPrincipal UserPrincipal user
    ) {
        IngestLinkedInJobResponse response = ingestionServiceGrpcClient.ingestLinkedInJob(
                UUID.fromString(user.userId()),
                request.jobId()
        );
        return ResponseEntity.ok(
                new LinkedInJobResponseDTO(response.getSuccess(), response.getMessage())
        );
    }


}
