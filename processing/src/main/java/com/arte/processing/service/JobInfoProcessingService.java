package com.arte.processing.service;

import com.arte.processing.entity.LinkedInJobs;
import com.arte.processing.entity.Users;
import com.arte.processing.exception.UserNotFoundException;
import com.arte.processing.grpc.ProcessJobInfoRequest;
import com.arte.processing.grpc.ProcessedJobData;
import com.arte.processing.processor.JobInfoProcessor;
import com.arte.processing.provider.LLMProvider;
import com.arte.processing.repository.LinkedInJobsRepository;
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
public class JobInfoProcessingService {

    private final LLMProvider llmProvider;
    private final UserRepository userRepository;
    private final LinkedInJobsRepository jobsRepository;
    private final JobInfoProcessor jobInfoProcessor;

    @Transactional
    public ProcessedJobData processJobInfo(ProcessJobInfoRequest request) throws IOException {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            String jobId = request.getJobId();
            
            log.info("Starting job processing for jobId: {} requested by user: {}", jobId, userId);

            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

            LinkedInJobs job = jobsRepository.findByJobId(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

            var model = llmProvider.getChatModel(user.getGithubToken());

            return jobInfoProcessor.process(job, model);

        } catch(Exception e) {
            log.error("Couldn't process job info for jobId: {}", request.getJobId(), e);
            throw new IOException(e);
        }
    }
}
