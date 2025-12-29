package com.arte.processing.processor;

import com.arte.processing.dto.response.ProcessedJobData;
import com.arte.processing.entity.LinkedInJobs;
import com.arte.processing.processor.prompts.JobInfoAssistant;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobInfoProcessor {

    public ProcessedJobData process(LinkedInJobs job, OpenAiOfficialChatModel model) {
        try {
            JobInfoAssistant assistant = AiServices.create(JobInfoAssistant.class, model);

            log.info("Processing job: {}", job.getJobId());
            
            ProcessedJobData result = assistant.analyzeJob(job.getRawContent());

            log.info("Successfully processed job: {}", job.getJobId());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error processing job: {}", job.getJobId(), e);
            throw new RuntimeException("Failed to process job", e);
        }
    }

}
