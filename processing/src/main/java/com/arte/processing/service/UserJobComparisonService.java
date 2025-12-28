package com.arte.processing.service;

import com.arte.processing.entity.Users;
import com.arte.processing.exception.UserNotFoundException;
import com.arte.processing.grpc.ProcessUserAndJobRequest;
import com.arte.processing.grpc.ProcessedJobData;
import com.arte.processing.grpc.ProcessedUserData;
import com.arte.processing.grpc.UserJobComparison;
import com.arte.processing.processor.UserJobComparisonProcessor;
import com.arte.processing.provider.LLMProvider;
import com.arte.processing.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserJobComparisonService {

    private final LLMProvider llmProvider;
    private final UserRepository userRepository;
    private final UserInfoProcessingService userInfoProcessingService;
    private final JobInfoProcessingService jobInfoProcessingService;
    private final UserJobComparisonProcessor comparisonProcessor;

    @Transactional
    public UserJobComparison compareUserAndJob(ProcessUserAndJobRequest request) throws IOException {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            String jobId = request.getJobId();
            
            log.info("Starting user-job comparison for user: {} and job: {}", userId, jobId);

            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

            dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel model = llmProvider.getChatModel(user.getGithubToken());

            ProcessedUserData userData = userInfoProcessingService.processUserInfo(
                    com.arte.processing.grpc.ProcessUserInfoRequest.newBuilder()
                            .setUserId(request.getUserId())
                            .build()
            );

            ProcessedJobData jobData = jobInfoProcessingService.processJobInfo(
                    com.arte.processing.grpc.ProcessJobInfoRequest.newBuilder()
                            .setUserId(request.getUserId())
                            .setJobId(jobId)
                            .build()
            );

            return comparisonProcessor.process(userData, jobData, model);

        } catch(Exception e) {
            log.error("Couldn't compare user {} with job {}", request.getUserId(), request.getJobId(), e);
            throw new IOException(e);
        }
    }
}
