package com.arte.ingestion.client;

import com.arte.ingestion.grpc.HealthCheckRequest;
import com.arte.ingestion.grpc.HealthCheckResponse;
import com.arte.ingestion.grpc.ProcessingServiceGrpc;
import com.arte.ingestion.grpc.TriggerRequest;
import com.arte.ingestion.grpc.TriggerResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ProcessingServiceGrpcClient {

    @Value("${processing.grpc.host:localhost}")
    private String grpcHost;

    @Value("${processing.grpc.port:50051}")
    private int grpcPort;

    @Value("${processing.grpc.timeout-seconds:30}")
    private int timeoutSeconds;

    private ManagedChannel channel;
    private ProcessingServiceGrpc.ProcessingServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        log.info("Initializing gRPC client for processing service at {}:{}", grpcHost, grpcPort);
        channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .usePlaintext() // Use TLS in production
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

    /**
     * Triggers embedding generation for the given knowledge base entries.
     *
     * @param userId            The user's UUID
     * @param sourceType        The source type (github, leetcode, resume)
     * @param knowledgeBaseIds  List of knowledge base entry IDs to process
     * @return TriggerResponse with success status and message
     */
    public TriggerResponse triggerEmbeddingGeneration(UUID userId, String sourceType, List<UUID> knowledgeBaseIds) {
        log.info("Triggering embedding generation for user {} with {} entries from {}",
                userId, knowledgeBaseIds.size(), sourceType);

        TriggerRequest request = TriggerRequest.newBuilder()
                .setUserId(userId.toString())
                .setSourceType(sourceType)
                .addAllKnowledgeBaseIds(knowledgeBaseIds.stream().map(UUID::toString).toList())
                .build();

        try {
            TriggerResponse response = blockingStub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .triggerEmbeddingGeneration(request);

            log.info("Embedding generation triggered: success={}, message={}, entriesQueued={}",
                    response.getSuccess(), response.getMessage(), response.getEntriesQueued());

            return response;
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed with status: {}", e.getStatus(), e);
            return TriggerResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("gRPC call failed: " + e.getStatus().getDescription())
                    .setEntriesQueued(0)
                    .build();
        }
    }

    /**
     * Health check for the processing service.
     *
     * @return true if the service is healthy, false otherwise
     */
    public boolean isHealthy() {
        try {
            HealthCheckResponse response = blockingStub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .healthCheck(HealthCheckRequest.newBuilder()
                            .setServiceName("ingestion")
                            .build());
            return response.getHealthy();
        } catch (StatusRuntimeException e) {
            log.warn("Health check failed: {}", e.getStatus());
            return false;
        }
    }
}
