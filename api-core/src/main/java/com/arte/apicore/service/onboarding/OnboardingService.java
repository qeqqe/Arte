package com.arte.apicore.service.onboarding;

import com.arte.apicore.client.IngestionServiceGrpcClient;
import com.arte.apicore.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class OnboardingService {
    private final UserRepository userRepository;
    private final IngestionServiceGrpcClient ingestionServiceGrpcClient;

    public OnboardingService(UserRepository userRepository, IngestionServiceGrpcClient ingestionServiceGrpcClient) {
        this.userRepository = userRepository;
        this.ingestionServiceGrpcClient = ingestionServiceGrpcClient;
    }




}
