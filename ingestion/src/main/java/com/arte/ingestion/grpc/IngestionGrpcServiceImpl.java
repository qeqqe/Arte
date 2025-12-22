package com.arte.ingestion.grpc;

import com.arte.ingestion.service.GitHubIngestionService;
import com.arte.ingestion.service.LeetCodeIngestionService;
import com.arte.ingestion.service.LinkedInJobIngestionService;
import com.arte.ingestion.service.ResumeProcessingService;
import com.arte.ingestion.util.ByteArrayMultipartFile;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class IngestionGrpcServiceImpl extends IngestionServiceGrpc.IngestionServiceImplBase {

    private final GitHubIngestionService gitHubIngestionService;
    private final LeetCodeIngestionService leetCodeIngestionService;
    private final ResumeProcessingService resumeProcessingService;
    private final LinkedInJobIngestionService linkedInJobIngestionService;

    public IngestionGrpcServiceImpl(
            GitHubIngestionService gitHubIngestionService,
            LeetCodeIngestionService leetCodeIngestionService,
            ResumeProcessingService resumeProcessingService, LinkedInJobIngestionService linkedInJobIngestionService) {
        this.gitHubIngestionService = gitHubIngestionService;
        this.leetCodeIngestionService = leetCodeIngestionService;
        this.resumeProcessingService = resumeProcessingService;
        this.linkedInJobIngestionService = linkedInJobIngestionService;
    }

    @Override
    public void ingestGitHub(IngestGitHubRequest request, StreamObserver<IngestGitHubResponse> responseObserver) {
        log.info("gRPC: Received GitHub ingestion request for user: {}", request.getUserId());
        
        try {
            UUID userId = UUID.fromString(request.getUserId());
            var result = gitHubIngestionService.ingestGitHubData(userId);
            
            var response = IngestGitHubResponse.newBuilder()
                    .setSuccess(result.success())
                    .setMessage(result.message())
                    .setReposProcessed(result.reposProcessed())
                    .addAllRepoNames(result.repoNames())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("gRPC: GitHub ingestion completed for user: {}", userId);
        } catch (Exception e) {
            log.error("gRPC: GitHub ingestion failed for user: {}", request.getUserId(), e);
            var response = IngestGitHubResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void ingestLeetCode(IngestLeetCodeRequest request, StreamObserver<IngestLeetCodeResponse> responseObserver) {
        log.info("gRPC: Received LeetCode ingestion for user: {}, leetcode: {}", 
                request.getUserId(), request.getLeetcodeUsername());
        
        try {
            UUID userId = UUID.fromString(request.getUserId());
            var result = leetCodeIngestionService.ingestLeetCodeData(userId, request.getLeetcodeUsername());
            
            var response = IngestLeetCodeResponse.newBuilder()
                    .setSuccess(result.success())
                    .setMessage(result.message())
                    .setProblemsSolved(result.problemsSolved())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("gRPC: LeetCode ingestion completed for user: {}", userId);
        } catch (Exception e) {
            log.error("gRPC: LeetCode ingestion failed for user: {}", request.getUserId(), e);
            var response = IngestLeetCodeResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void ingestResume(IngestResumeRequest request, StreamObserver<IngestResumeResponse> responseObserver) {
        log.info("gRPC: Received resume ingestion for user: {}, filename: {}", 
                request.getUserId(), request.getFilename());
        
        try {
            UUID userId = UUID.fromString(request.getUserId());
            
            var file = new ByteArrayMultipartFile(
                    request.getContent().toByteArray(),
                    "file", 
                    request.getFilename(), 
                    "application/pdf"
            );
            
            var result = resumeProcessingService.processResume(userId, file);
            
            var response = IngestResumeResponse.newBuilder()
                    .setSuccess(result.success())
                    .setMessage(result.message())
                    .setWordCount(result.wordCount())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("gRPC: Resume ingestion completed for user: {}", userId);
        } catch (Exception e) {
            log.error("gRPC: Resume ingestion failed for user: {}", request.getUserId(), e);
            var response = IngestResumeResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void ingestAll(IngestAllRequest request, StreamObserver<IngestAllResponse> responseObserver) {
        log.info("gRPC: Received full ingestion for user: {}", request.getUserId());
        
        IngestGitHubResponse githubResponse = null;
        IngestLeetCodeResponse leetcodeResponse = null;
        IngestResumeResponse resumeResponse = null;
        boolean overallSuccess = true;
        
        try {
            UUID userId = UUID.fromString(request.getUserId());
            
            // GitHub ingestion
            try {
                var githubResult = gitHubIngestionService.ingestGitHubData(userId);
                githubResponse = IngestGitHubResponse.newBuilder()
                        .setSuccess(githubResult.success())
                        .setMessage(githubResult.message())
                        .setReposProcessed(githubResult.reposProcessed())
                        .addAllRepoNames(githubResult.repoNames())
                        .build();
            } catch (Exception e) {
                log.warn("GitHub ingestion failed for user: {}", userId, e);
                githubResponse = IngestGitHubResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Error: " + e.getMessage())
                        .build();
                overallSuccess = false;
            }
            
            // LeetCode ingestion (only if username provided)
            if (!request.getLeetcodeUsername().isEmpty()) {
                try {
                    var leetcodeResult = leetCodeIngestionService.ingestLeetCodeData(
                            userId, request.getLeetcodeUsername());
                    leetcodeResponse = IngestLeetCodeResponse.newBuilder()
                            .setSuccess(leetcodeResult.success())
                            .setMessage(leetcodeResult.message())
                            .setProblemsSolved(leetcodeResult.problemsSolved())
                            .build();
                } catch (Exception e) {
                    log.warn("LeetCode ingestion failed for user: {}", userId, e);
                    leetcodeResponse = IngestLeetCodeResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("Error: " + e.getMessage())
                            .build();
                }
            }
            
            // Resume ingestion (only if content provided)
            if (!request.getResumeContent().isEmpty() && !request.getResumeFilename().isEmpty()) {
                try {
                    var file = new ByteArrayMultipartFile(
                            request.getResumeContent().toByteArray(),
                            "file",
                            request.getResumeFilename(),
                            "application/pdf"
                    );
                    var resumeResult = resumeProcessingService.processResume(userId, file);
                    resumeResponse = IngestResumeResponse.newBuilder()
                            .setSuccess(resumeResult.success())
                            .setMessage(resumeResult.message())
                            .setWordCount(resumeResult.wordCount())
                            .build();
                } catch (Exception e) {
                    log.warn("Resume ingestion failed for user: {}", userId, e);
                    resumeResponse = IngestResumeResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("Error: " + e.getMessage())
                            .build();
                }
            }
            
            var responseBuilder = IngestAllResponse.newBuilder()
                    .setSuccess(overallSuccess)
                    .setMessage(overallSuccess ? "Full ingestion completed" : "Some ingestions failed");

            if (githubResponse != null) responseBuilder.setGithubResult(githubResponse);
            if (leetcodeResponse != null) responseBuilder.setLeetcodeResult(leetcodeResponse);
            if (resumeResponse != null) responseBuilder.setResumeResult(resumeResponse);
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            log.info("gRPC: Full ingestion completed for user: {}", userId);
        } catch (Exception e) {
            log.error("gRPC: Full ingestion failed for user: {}", request.getUserId(), e);
            var response = IngestAllResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void healthCheck(IngestionHealthRequest request, StreamObserver<IngestionHealthResponse> responseObserver) {
        log.debug("gRPC: Health check requested by: {}", request.getServiceName());
        
        var response = IngestionHealthResponse.newBuilder()
                .setHealthy(true)
                .setStatus("Ingestion service is healthy")
                .setTimestamp(System.currentTimeMillis())
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ingestLinkedInJob(IngestLinkedInJobRequest request,StreamObserver<IngestLinkedInJobResponse> responseObserver) {
        log.info("gRPC: Received job ingestion for user: {}, jobId: {}",
                request.getUserId(), request.getJobId());

        try {
            UUID userId = UUID.fromString(request.getUserId());
            var result = linkedInJobIngestionService.ingestLinkedInJob(userId, request.getJobId());

            var response = IngestLinkedInJobResponse.newBuilder()
                    .setSuccess(result.success())
                    .setMessage(result.message())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC: ingestion Job completed for user: {}, jobId: {}",
                    request.getUserId(), request.getJobId());

        } catch (Exception e) {
            log.error("gRPC: Job ingestion failed for user: {}", request.getUserId(), e);
            var response = IngestLinkedInJobResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
