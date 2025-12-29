package com.arte.processing.service;

import com.arte.processing.dto.response.ProcessedJobData;
import com.arte.processing.entity.LinkedInJobs;
import com.arte.processing.entity.Users;
import com.arte.processing.exception.UserNotFoundException;
import com.arte.processing.processor.JobInfoProcessor;
import com.arte.processing.provider.LLMProvider;
import com.arte.processing.repository.LinkedInJobsRepository;
import com.arte.processing.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobInfoProcessingService {

    private static final String DEFAULT_VERSION = "v1";

    private final LLMProvider llmProvider;
    private final UserRepository userRepository;
    private final LinkedInJobsRepository jobsRepository;
    private final JobInfoProcessor jobInfoProcessor;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProcessedJobData processJobInfo(UUID userId, String jobId, String processingVersion) {
        log.info("Starting job processing for jobId: {} requested by user: {}", jobId, userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        LinkedInJobs job = jobsRepository.findByJobId(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        var model = llmProvider.getChatModel(user.getGithubToken());

        ProcessedJobData result = jobInfoProcessor.process(job, model);

        persistProcessedJobData(job, result, processingVersion);

        log.info("Successfully processed job: {}", jobId);
        return result;
    }

    @SuppressWarnings("unchecked")
    private void persistProcessedJobData(LinkedInJobs job, ProcessedJobData data, String version) {
        var dataMap = objectMapper.convertValue(data, java.util.Map.class);
        job.setProcessedJobData(dataMap);
        job.setProcessingVersion(version != null ? version : DEFAULT_VERSION);
        job.setProcessedAt(Instant.now());
        jobsRepository.save(job);
        log.debug("Persisted processed job data for job: {}", job.getJobId());
    }
}
