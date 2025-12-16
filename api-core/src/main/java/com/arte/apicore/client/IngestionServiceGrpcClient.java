package com.arte.apicore.client;

import com.arte.apicore.grpc.*;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

// grpc client for calling ingestion service
@Component
@Slf4j
public class IngestionServiceGrpcClient {

    @Value("${ingestion.grpc.host:localhost}")
    private String grpcHost;

    @Value("${ingestion.grpc.port:50052}")
    private int grpcPort;

    @Value("${ingestion.grpc.timeout-seconds:30}")
    private int timeoutSeconds;

    private ManagedChannel channel;
    private IngestionServiceGrpc.IngestionServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        log.info("connecting to ingestion service at {}:{}", grpcHost, grpcPort);
        channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .usePlaintext()
                .build();
        blockingStub = IngestionServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("grpc channel shutdown interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    // triggers github data ingestion for a user
    public IngestGitHubResponse ingestGitHub(UUID userId) {
        log.info("triggering github ingestion for user: {}", userId);

        IngestGitHubRequest request = IngestGitHubRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        try {
            return blockingStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .ingestGitHub(request);
        } catch (StatusRuntimeException e) {
            log.error("grpc call failed: {}", e.getStatus(), e);
            return IngestGitHubResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("grpc error: " + e.getStatus().getDescription())
                    .build();
        }
    }

    // triggers leetcode data ingestion for a user
    public IngestLeetCodeResponse ingestLeetCode(UUID userId, String leetcodeUsername) {
        log.info("triggering leetcode ingestion for user: {} (leetcode: {})", userId, leetcodeUsername);

        IngestLeetCodeRequest request = IngestLeetCodeRequest.newBuilder()
                .setUserId(userId.toString())
                .setLeetcodeUsername(leetcodeUsername)
                .build();

        try {
            return blockingStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .ingestLeetCode(request);
        } catch (StatusRuntimeException e) {
            log.error("grpc call failed: {}", e.getStatus(), e);
            return IngestLeetCodeResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("grpc error: " + e.getStatus().getDescription())
                    .build();
        }
    }

    // triggers resume processing for a user
    public IngestResumeResponse ingestResume(UUID userId, String filename, byte[] content) {
        log.info("triggering resume ingestion for user: {}, file: {}", userId, filename);

        IngestResumeRequest request = IngestResumeRequest.newBuilder()
                .setUserId(userId.toString())
                .setFilename(filename)
                .setContent(ByteString.copyFrom(content))
                .build();

        try {
            return blockingStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .ingestResume(request);
        } catch (StatusRuntimeException e) {
            log.error("grpc call failed: {}", e.getStatus(), e);
            return IngestResumeResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("grpc error: " + e.getStatus().getDescription())
                    .build();
        }
    }

    // triggers all ingestion types for a user
    public IngestAllResponse ingestAll(UUID userId, String leetcodeUsername, byte[] resumeContent, String resumeFilename) {
        log.info("triggering full ingestion for user: {}", userId);

        var requestBuilder = IngestAllRequest.newBuilder()
                .setUserId(userId.toString());

        if (leetcodeUsername != null && !leetcodeUsername.isBlank()) {
            requestBuilder.setLeetcodeUsername(leetcodeUsername);
        }
        if (resumeContent != null && resumeFilename != null) {
            requestBuilder.setResumeContent(ByteString.copyFrom(resumeContent));
            requestBuilder.setResumeFilename(resumeFilename);
        }

        try {
            return blockingStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .ingestAll(requestBuilder.build());
        } catch (StatusRuntimeException e) {
            log.error("grpc call failed: {}", e.getStatus(), e);
            return IngestAllResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("grpc error: " + e.getStatus().getDescription())
                    .build();
        }
    }

    // checks if ingestion service is healthy
    public boolean isHealthy() {
        try {
            IngestionHealthResponse response = blockingStub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .healthCheck(IngestionHealthRequest.newBuilder()
                            .setServiceName("api-core")
                            .build());
            return response.getHealthy();
        } catch (StatusRuntimeException e) {
            log.warn("ingestion service health check failed: {}", e.getStatus());
            return false;
        }
    }
}
