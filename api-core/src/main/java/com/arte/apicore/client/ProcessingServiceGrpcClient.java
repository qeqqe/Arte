package com.arte.apicore.client;

import com.arte.apicore.grpc.processing.*;
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

@Component
@Slf4j
public class ProcessingServiceGrpcClient {

    @Value("${processing.grpc.host:localhost}")
    private String grpcHost;

    @Value("${processing.grpc.port:50053}")
    private int grpcPort;

    @Value("${processing.grpc.timeout-seconds:120}")
    private int timeoutSeconds;

    private ManagedChannel channel;
    private ProcessingServiceGrpc.ProcessingServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        log.info("Connecting to processing service at {}:{}", grpcHost, grpcPort);
        channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .usePlaintext()
                .build();
        blockingStub = ProcessingServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("gRPC channel shutdown interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public ProcessUserInfoResponse processUserInfo(UUID userId) {
        log.info("Triggering user info processing for user: {}", userId);

        ProcessUserInfoRequest request = ProcessUserInfoRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        try {
            return blockingStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .processUserInfo(request);
        } catch (StatusRuntimeException e) {
            handleGrpcError(e, userId, "User-Info");
            return ProcessUserInfoResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Processing failed: " + e.getStatus().getDescription())
                    .build();
        }
    }

    public ProcessJobInfoResponse processJobInfo(UUID userId, String jobId) {
        log.info("Triggering job info processing for user: {}, job: {}", userId, jobId);

        ProcessJobInfoRequest request = ProcessJobInfoRequest.newBuilder()
                .setUserId(userId.toString())
                .setJobId(jobId)
                .build();

        try {
            return blockingStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .processJobInfo(request);
        } catch (StatusRuntimeException e) {
            handleGrpcError(e, userId, "Job-Info");
            return ProcessJobInfoResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("gRPC error: " + e.getStatus().getDescription())
                    .build();
        }
    }

    public ProcessUserAndJobResponse compareUserAndJob(UUID userId, String jobId) {
        log.info("Triggering user-job comparison for user: {}, job: {}", userId, jobId);

        ProcessUserAndJobRequest request = ProcessUserAndJobRequest.newBuilder()
                .setUserId(userId.toString())
                .setJobId(jobId)
                .build();

        try {
            return blockingStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .processUserAndJob(request);
        } catch (StatusRuntimeException e) {
            handleGrpcError(e, userId, "User-Job Comparison");
            return ProcessUserAndJobResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("gRPC error: " + e.getStatus().getDescription())
                    .build();
        }
    }

    private void handleGrpcError(StatusRuntimeException e, UUID userId, String service) {
        switch (e.getStatus().getCode()) {
            case DEADLINE_EXCEEDED:
                log.warn("{} Processing timeout for user {}", service, userId);
                break;
            case UNAVAILABLE:
                log.warn("{} Processing service unavailable for user {}", service, userId);
                break;
            default:
                log.error("{} Unexpected gRPC error for user {}: {}", service, userId, e.getStatus(), e);
        }
    }

    public boolean isHealthy() {
        try {
            ProcessUserInfoRequest healthCheck = ProcessUserInfoRequest.newBuilder()
                    .setUserId("health-check")
                    .build();
            blockingStub.withDeadlineAfter(5, TimeUnit.SECONDS);
            return true;
        } catch (StatusRuntimeException e) {
            log.warn("Processing service health check failed: {}", e.getStatus());
            return false;
        }
    }
}
