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

    @Value("${processing.grpc.timeout-seconds:60}")
    private int timeoutSeconds;

    private ManagedChannel channel;
    private ProcessingServiceGrpc.ProcessingServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        log.info("connecting to processing service at {}:{}", grpcHost, grpcPort);
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
                log.warn("grpc channel shutdown interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public ProcessUserInfoResponse processUserInfo(UUID userId, UserInfoData userInfo, String processingVersion) {
        log.info("triggering user info processing for user: {}", userId);

        ProcessUserInfoRequest request = ProcessUserInfoRequest.newBuilder()
                .setUserId(userId.toString())
                .setUserInfo(userInfo)
                .setProcessingVersion(processingVersion != null ? processingVersion : "v1")
                .build();

        try {
            return blockingStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .processUserInfo(request);
        } catch (StatusRuntimeException e) {
            log.error("grpc call failed: {}", e.getStatus(), e);
            return ProcessUserInfoResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("grpc error: " + e.getStatus().getDescription())
                    .build();
        }
    }

    public ProcessJobInfoResponse processJobInfo(String jobId, String rawJobMarkdown, String processingVersion) {
        log.info("triggering job info processing for job: {}", jobId);

        ProcessJobInfoRequest request = ProcessJobInfoRequest.newBuilder()
                .setJobId(jobId)
                .setRawJobMarkdown(rawJobMarkdown)
                .setProcessingVersion(processingVersion != null ? processingVersion : "v1")
                .build();

        try {
            return blockingStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .processJobInfo(request);
        } catch (StatusRuntimeException e) {
            log.error("grpc call failed: {}", e.getStatus(), e);
            return ProcessJobInfoResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("grpc error: " + e.getStatus().getDescription())
                    .build();
        }
    }

    public ProcessUserAndJobResponse processUserAndJob(
            UUID userId, 
            ProcessedUserData processedUser,
            String jobId,
            ProcessedJobData processedJob,
            String processingVersion) {
        log.info("triggering user-job comparison for user: {}, job: {}", userId, jobId);

        ProcessUserAndJobRequest request = ProcessUserAndJobRequest.newBuilder()
                .setUserId(userId.toString())
                .setProcessedUser(processedUser)
                .setJobId(jobId)
                .setProcessedJob(processedJob)
                .setProcessingVersion(processingVersion != null ? processingVersion : "v1")
                .build();

        try {
            return blockingStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .processUserAndJob(request);
        } catch (StatusRuntimeException e) {
            log.error("grpc call failed: {}", e.getStatus(), e);
            return ProcessUserAndJobResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("grpc error: " + e.getStatus().getDescription())
                    .build();
        }
    }
}
