package com.arte.ingestion.grpc;

import com.arte.ingestion.service.GitHubIngestionService;
import com.arte.ingestion.service.LeetCodeIngestionService;
import com.arte.ingestion.service.ResumeProcessingService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionGrpcServiceImplTest {

    @Mock private GitHubIngestionService gitHubIngestionService;
    @Mock private LeetCodeIngestionService leetCodeIngestionService;
    @Mock private ResumeProcessingService resumeProcessingService;
    @Mock private StreamObserver<IngestGitHubResponse> githubObserver;
    @Mock private StreamObserver<IngestLeetCodeResponse> leetcodeObserver;
    @Mock private StreamObserver<IngestionHealthResponse> healthObserver;

    private IngestionGrpcServiceImpl grpcService;

    @BeforeEach
    void setUp() {
        grpcService = new IngestionGrpcServiceImpl(
                gitHubIngestionService,
                leetCodeIngestionService,
                resumeProcessingService
        );
    }

    @Test
    void ingestGitHub_success_returnsCorrectResponse() {
        UUID userId = UUID.randomUUID();
        var result = new GitHubIngestionService.IngestionResult(
                true, "Success", 3, List.of("repo1", "repo2", "repo3")
        );
        when(gitHubIngestionService.ingestGitHubData(userId)).thenReturn(result);

        var request = IngestGitHubRequest.newBuilder()
                .setUserId(userId.toString())
                .build();
        grpcService.ingestGitHub(request, githubObserver);

        ArgumentCaptor<IngestGitHubResponse> captor = ArgumentCaptor.forClass(IngestGitHubResponse.class);
        verify(githubObserver).onNext(captor.capture());
        verify(githubObserver).onCompleted();
        
        var response = captor.getValue();
        assertTrue(response.getSuccess());
        assertEquals(3, response.getReposProcessed());
        assertEquals(3, response.getRepoNamesCount());
    }

    @Test
    void ingestGitHub_failure_returnsErrorResponse() {
        UUID userId = UUID.randomUUID();
        when(gitHubIngestionService.ingestGitHubData(any(UUID.class)))
                .thenThrow(new IllegalArgumentException("User not found"));

        var request = IngestGitHubRequest.newBuilder()
                .setUserId(userId.toString())
                .build();
        grpcService.ingestGitHub(request, githubObserver);

        ArgumentCaptor<IngestGitHubResponse> captor = ArgumentCaptor.forClass(IngestGitHubResponse.class);
        verify(githubObserver).onNext(captor.capture());
        verify(githubObserver).onCompleted();
        
        var response = captor.getValue();
        assertFalse(response.getSuccess());
        assertTrue(response.getMessage().contains("User not found"));
    }

    @Test
    void ingestLeetCode_success_returnsCorrectResponse() {
        UUID userId = UUID.randomUUID();
        var result = new LeetCodeIngestionService.IngestionResult(true, "Success", 150);
        when(leetCodeIngestionService.ingestLeetCodeData(any(UUID.class), eq("testuser")))
                .thenReturn(result);

        var request = IngestLeetCodeRequest.newBuilder()
                .setUserId(userId.toString())
                .setLeetcodeUsername("testuser")
                .build();
        grpcService.ingestLeetCode(request, leetcodeObserver);

        ArgumentCaptor<IngestLeetCodeResponse> captor = ArgumentCaptor.forClass(IngestLeetCodeResponse.class);
        verify(leetcodeObserver).onNext(captor.capture());
        verify(leetcodeObserver).onCompleted();
        
        var response = captor.getValue();
        assertTrue(response.getSuccess());
        assertEquals(150, response.getProblemsSolved());
    }

    @Test
    void healthCheck_returnsHealthyResponse() {
        var request = IngestionHealthRequest.newBuilder()
                .setServiceName("api-core")
                .build();
        grpcService.healthCheck(request, healthObserver);

        ArgumentCaptor<IngestionHealthResponse> captor = ArgumentCaptor.forClass(IngestionHealthResponse.class);
        verify(healthObserver).onNext(captor.capture());
        verify(healthObserver).onCompleted();
        
        var response = captor.getValue();
        assertTrue(response.getHealthy());
        assertTrue(response.getTimestamp() > 0);
    }
}
