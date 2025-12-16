package com.arte.apicore.client;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.arte.apicore.grpc.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

// tests for grpc client that calls ingestion service
class IngestionServiceGrpcClientTest {

    private Server server;
    private ManagedChannel channel;
    private IngestionServiceGrpcClient client;
    private MockIngestionService mockService;
    private final String serverName = InProcessServerBuilder.generateName();

    @BeforeEach
    void setUp() throws Exception {
        mockService = new MockIngestionService();
        
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(mockService)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        client = new IngestionServiceGrpcClient();
        ReflectionTestUtils.setField(client, "grpcHost", "localhost");
        ReflectionTestUtils.setField(client, "grpcPort", 0);
        ReflectionTestUtils.setField(client, "timeoutSeconds", 5);
        
        // inject the test channel directly
        ReflectionTestUtils.setField(client, "channel", channel);
        ReflectionTestUtils.setField(client, "blockingStub", IngestionServiceGrpc.newBlockingStub(channel));
    }

    @AfterEach
    void tearDown() throws Exception {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void ingestGitHub_success() {
        UUID userId = UUID.randomUUID();
        mockService.setGitHubResponse(true, "github data ingested", 5);

        var response = client.ingestGitHub(userId);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("github data ingested");
    }

    @Test
    void ingestLeetCode_success() {
        UUID userId = UUID.randomUUID();
        mockService.setLeetCodeResponse(true, "leetcode data ingested", 42);

        var response = client.ingestLeetCode(userId, "testuser");

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("leetcode data ingested");
    }

    @Test
    void ingestResume_success() {
        UUID userId = UUID.randomUUID();
        byte[] pdfData = "fake pdf content".getBytes();
        mockService.setResumeResponse(true, "resume processed", 500);

        var response = client.ingestResume(userId, "resume.pdf", pdfData);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("resume processed");
    }

    @Test
    void ingestAll_success() {
        UUID userId = UUID.randomUUID();
        byte[] pdfData = "fake pdf content".getBytes();
        mockService.setAllResponse(true, "all data ingested");

        var response = client.ingestAll(userId, "leetcodeuser", pdfData, "resume.pdf");

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("all data ingested");
    }

    @Test
    void isHealthy_returnsTrue_whenServiceHealthy() {
        mockService.setHealthResponse(true);

        boolean result = client.isHealthy();

        assertThat(result).isTrue();
    }

    @Test
    void isHealthy_returnsFalse_whenServiceUnhealthy() {
        mockService.setHealthResponse(false);

        boolean result = client.isHealthy();

        assertThat(result).isFalse();
    }

    @Test
    void ingestGitHub_failure_response() {
        UUID userId = UUID.randomUUID();
        mockService.setGitHubResponse(false, "user not found", 0);

        var response = client.ingestGitHub(userId);

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("user not found");
    }

    // mock grpc service for testing
    private static class MockIngestionService extends IngestionServiceGrpc.IngestionServiceImplBase {
        private IngestGitHubResponse gitHubResponse;
        private IngestLeetCodeResponse leetCodeResponse;
        private IngestResumeResponse resumeResponse;
        private IngestAllResponse allResponse;
        private IngestionHealthResponse healthResponse;

        void setGitHubResponse(boolean success, String message, int reposProcessed) {
            this.gitHubResponse = IngestGitHubResponse.newBuilder()
                    .setSuccess(success)
                    .setMessage(message)
                    .setReposProcessed(reposProcessed)
                    .build();
        }

        void setLeetCodeResponse(boolean success, String message, int problemsSolved) {
            this.leetCodeResponse = IngestLeetCodeResponse.newBuilder()
                    .setSuccess(success)
                    .setMessage(message)
                    .setProblemsSolved(problemsSolved)
                    .build();
        }

        void setResumeResponse(boolean success, String message, int wordCount) {
            this.resumeResponse = IngestResumeResponse.newBuilder()
                    .setSuccess(success)
                    .setMessage(message)
                    .setWordCount(wordCount)
                    .build();
        }

        void setAllResponse(boolean success, String message) {
            this.allResponse = IngestAllResponse.newBuilder()
                    .setSuccess(success)
                    .setMessage(message)
                    .build();
        }

        void setHealthResponse(boolean healthy) {
            this.healthResponse = IngestionHealthResponse.newBuilder()
                    .setHealthy(healthy)
                    .setStatus(healthy ? "ok" : "unhealthy")
                    .build();
        }

        @Override
        public void ingestGitHub(IngestGitHubRequest request, StreamObserver<IngestGitHubResponse> responseObserver) {
            responseObserver.onNext(gitHubResponse);
            responseObserver.onCompleted();
        }

        @Override
        public void ingestLeetCode(IngestLeetCodeRequest request, StreamObserver<IngestLeetCodeResponse> responseObserver) {
            responseObserver.onNext(leetCodeResponse);
            responseObserver.onCompleted();
        }

        @Override
        public void ingestResume(IngestResumeRequest request, StreamObserver<IngestResumeResponse> responseObserver) {
            responseObserver.onNext(resumeResponse);
            responseObserver.onCompleted();
        }

        @Override
        public void ingestAll(IngestAllRequest request, StreamObserver<IngestAllResponse> responseObserver) {
            responseObserver.onNext(allResponse);
            responseObserver.onCompleted();
        }

        @Override
        public void healthCheck(IngestionHealthRequest request, StreamObserver<IngestionHealthResponse> responseObserver) {
            responseObserver.onNext(healthResponse);
            responseObserver.onCompleted();
        }
    }
}
