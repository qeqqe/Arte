package com.arte.jobhunter.grpc;

import com.arte.jobhunter.dto.FetchJobRequest;
import com.arte.jobhunter.feature.jobscraper.JobScraperHandler;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@Slf4j
public class JobHunterGrpcServiceImpl extends JobHunterServiceGrpc.JobHunterServiceImplBase {
    private final JobScraperHandler jobScraperHandler;

    public JobHunterGrpcServiceImpl(JobScraperHandler jobScraperHandler) {
        this.jobScraperHandler = jobScraperHandler;
    }

    @Override
    public void fetchJob(FetchJobRequest request, StreamObserver<FetchJobResponse> responseObserver) {

    }
}