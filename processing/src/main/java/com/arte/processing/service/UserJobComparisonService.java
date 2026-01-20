package com.arte.processing.service;

import com.arte.processing.dto.response.ProcessedJobData;
import com.arte.processing.dto.response.ProcessedUserData;
import com.arte.processing.dto.response.UserJobComparison;
import com.arte.processing.entity.LinkedInJobs;
import com.arte.processing.entity.UserInfo;
import com.arte.processing.entity.UserJobComparisons;
import com.arte.processing.entity.Users;
import com.arte.processing.exception.JobNotFoundException;
import com.arte.processing.exception.UserNotFoundException;
import com.arte.processing.processor.UserJobComparisonProcessor;
import com.arte.processing.provider.LLMProvider;
import com.arte.processing.repository.LinkedInJobsRepository;
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
    private final LinkedInJobsRepository linkedInJobsRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public UserJobComparison compareUserAndJob(UUID userId, String jobId) {
        log.info("Starting user-job comparison for user: {} and job: {}", userId, jobId);

        Users user = fetchUser(userId);
        LinkedInJobs job = fetchJob(jobId);

        ProcessedUserData userData = getOrProcessUserData(user);
        ProcessedJobData jobData = getOrProcessJobData(userId, job, DEFAULT_VERSION);

        var model = llmProvider.getChatModel(user.getGithubToken());
        UserJobComparison result = comparisonProcessor.process(userData, jobData, model);

        String processingVersion = getExistingProcessingVersion(userId, jobId);
        persistComparison(user, jobId, result, processingVersion);

        log.info("Successfully compared user {} with job {}", userId, jobId);
        return result;
    }

    private Users fetchUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    private LinkedInJobs fetchJob(String jobId) {
        return linkedInJobsRepository.findByJobId(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found: " + jobId));
    }

    private ProcessedUserData getOrProcessUserData(Users user) {
        UserInfo userInfo = user.getUserInfo();
        ProcessedUserData userData = userInfo.getProcessedUserData();

        if (userData == null) {
            log.debug("Processing user data for user: {}", user.getId());
            userData = (userInfoProcessingService.processUserInfo(user)).toUserData();
        }

        return userData;
    }

    private ProcessedJobData getOrProcessJobData(UUID userId, LinkedInJobs job, String version) {
        ProcessedJobData jobData = job.getProcessedJobData();

        if (jobData == null) {
            log.debug("Processing job data for job: {}", job.getJobId());
            jobData = jobInfoProcessingService.processJobInfo(userId, job.getJobId(), version);
        }

        return jobData;
    }

    private String getExistingProcessingVersion(UUID userId, String jobId) {
        return comparisonsRepository
                .findByUserIdAndJobId(userId, jobId)
                .map(UserJobComparisons::getProcessingVersion)
                .orElse(DEFAULT_VERSION);
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