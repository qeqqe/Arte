package com.arte.processing.service;

import com.arte.processing.dto.response.ProcessedJobData;
import com.arte.processing.dto.response.ProcessedUserData;
import com.arte.processing.dto.response.UserJobComparison;
import com.arte.processing.entity.UserInfo;
import com.arte.processing.entity.UserJobComparisons;
import com.arte.processing.entity.Users;
import com.arte.processing.exception.UserNotFoundException;
import com.arte.processing.processor.UserJobComparisonProcessor;
import com.arte.processing.provider.LLMProvider;
import com.arte.processing.repository.UserJobComparisonsRepository;
import com.arte.processing.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserJobComparisonService {

    private static final String DEFAULT_VERSION = "v1";

    private final LLMProvider llmProvider;
    private final UserRepository userRepository;
    private final UserInfoProcessingService userInfoProcessingService;
    private final JobInfoProcessingService jobInfoProcessingService;
    private final UserJobComparisonProcessor comparisonProcessor;
    private final UserJobComparisonsRepository comparisonsRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public UserJobComparison compareUserAndJob(UUID userId, String jobId, String processingVersion) {
        log.info("Starting user-job comparison for user: {} and job: {}", userId, jobId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        var model = llmProvider.getChatModel(user.getGithubToken());
        UserInfo userInfo = user.getUserInfo();
        ProcessedUserData userData = userInfo.getProcessedUserData();
        if(userData == null){
            userData = userInfoProcessingService.processUserInfo(userId);
        }
        ProcessedJobData jobData = jobInfoProcessingService.processJobInfo(userId, jobId, processingVersion);

        UserJobComparison result = comparisonProcessor.process(userData, jobData, model);

        persistComparison(user, jobId, result, processingVersion);

        log.info("Successfully compared user {} with job {}", userId, jobId);
        return result;
    }

    @SuppressWarnings("unchecked")
    private void persistComparison(Users user, String jobId, UserJobComparison comparison, String version) {
        var dataMap = objectMapper.convertValue(comparison, java.util.Map.class);

        UserJobComparisons entity = comparisonsRepository
                .findByUserIdAndJobId(user.getId(), jobId)
                .orElse(UserJobComparisons.builder()
                        .user(user)
                        .jobId(jobId)
                        .build());

        entity.setComparisonData(dataMap);
        entity.setMatchScore(BigDecimal.valueOf(comparison.overallMatchScore()));
        entity.setProcessingVersion(version != null ? version : DEFAULT_VERSION);

        comparisonsRepository.save(entity);
        log.debug("Persisted comparison for user: {} and job: {}", user.getId(), jobId);
    }
}
