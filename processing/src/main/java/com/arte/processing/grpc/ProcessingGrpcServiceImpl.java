package com.arte.processing.grpc;

import com.arte.processing.dto.response.ProcessedJobData;
import com.arte.processing.dto.response.ProcessedUserData;
import com.arte.processing.dto.response.UserJobComparison;
import com.arte.processing.mapper.ProcessingProtoMapper;
import com.arte.processing.service.JobInfoProcessingService;
import com.arte.processing.service.UserInfoProcessingService;
import com.arte.processing.service.UserJobComparisonService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class ProcessingGrpcServiceImpl extends ProcessingServiceGrpc.ProcessingServiceImplBase {

    private final UserInfoProcessingService userInfoProcessingService;
    private final JobInfoProcessingService jobInfoProcessingService;
    private final UserJobComparisonService userJobComparisonService;

    @Override
    public void processUserInfo(ProcessUserInfoRequest request, StreamObserver<ProcessUserInfoResponse> responseObserver) {
        log.info("gRPC: Processing user info for user: {}", request.getUserId());

        try {
            UUID userId = UUID.fromString(request.getUserId());

            ProcessedUserData result = userInfoProcessingService.processUserInfo(userId);

            var response = ProcessUserInfoResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User info processed successfully")
                    .setProcessedData(ProcessingProtoMapper.toProto(result))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("gRPC: User info processing completed for user: {}", userId);

        } catch (IllegalArgumentException e) {
            log.error("gRPC: Invalid user ID format: {}", request.getUserId(), e);
            sendErrorResponse(responseObserver, "Invalid user ID format");
        } catch (Exception e) {
            log.error("gRPC: Failed to process user info for user: {}", request.getUserId(), e);
            sendErrorResponse(responseObserver, "Processing failed: " + e.getMessage());
        }
    }

    @Override
    public void processJobInfo(ProcessJobInfoRequest request, StreamObserver<ProcessJobInfoResponse> responseObserver) {
        log.info("gRPC: Processing job info for user: {}, job: {}", request.getUserId(), request.getJobId());

        try {
            UUID userId = UUID.fromString(request.getUserId());
            String jobId = request.getJobId();

            ProcessedJobData result = jobInfoProcessingService.processJobInfo(userId, jobId, "v1");

            var response = ProcessJobInfoResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Job info processed successfully")
                    .setProcessedData(ProcessingProtoMapper.toProto(result))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("gRPC: Job info processing completed for job: {}", jobId);

        } catch (IllegalArgumentException e) {
            log.error("gRPC: Invalid request parameters", e);
            sendJobErrorResponse(responseObserver, "Invalid request: " + e.getMessage());
        } catch (Exception e) {
            log.error("gRPC: Failed to process job info for job: {}", request.getJobId(), e);
            sendJobErrorResponse(responseObserver, "Processing failed: " + e.getMessage());
        }
    }

    @Override
    public void processUserAndJob(ProcessUserAndJobRequest request, StreamObserver<ProcessUserAndJobResponse> responseObserver) {
        log.info("gRPC: Processing user-job comparison for user: {}, job: {}", request.getUserId(), request.getJobId());

        try {
            UUID userId = UUID.fromString(request.getUserId());
            String jobId = request.getJobId();

            UserJobComparison result = userJobComparisonService.compareUserAndJob(userId, jobId, "v1");

            var response = ProcessUserAndJobResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User-job comparison completed successfully")
                    .setComparison(ProcessingProtoMapper.toProto(result))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("gRPC: User-job comparison completed for user: {}, job: {}", userId, jobId);

        } catch (IllegalArgumentException e) {
            log.error("gRPC: Invalid request parameters", e);
            sendComparisonErrorResponse(responseObserver, "Invalid request: " + e.getMessage());
        } catch (Exception e) {
            log.error("gRPC: Failed to compare user {} with job {}", request.getUserId(), request.getJobId(), e);
            sendComparisonErrorResponse(responseObserver, "Comparison failed: " + e.getMessage());
        }
    }

    private void sendErrorResponse(StreamObserver<ProcessUserInfoResponse> observer, String message) {
        observer.onNext(ProcessUserInfoResponse.newBuilder()
                .setSuccess(false)
                .setMessage(message)
                .build());
        observer.onCompleted();
    }

    private void sendJobErrorResponse(StreamObserver<ProcessJobInfoResponse> observer, String message) {
        observer.onNext(ProcessJobInfoResponse.newBuilder()
                .setSuccess(false)
                .setMessage(message)
                .build());
        observer.onCompleted();
    }

    private void sendComparisonErrorResponse(StreamObserver<ProcessUserAndJobResponse> observer, String message) {
        observer.onNext(ProcessUserAndJobResponse.newBuilder()
                .setSuccess(false)
                .setMessage(message)
                .build());
        observer.onCompleted();
    }
}
