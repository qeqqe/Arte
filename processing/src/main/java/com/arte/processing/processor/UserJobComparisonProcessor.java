package com.arte.processing.processor;

import com.arte.processing.dto.response.ProcessedJobData;
import com.arte.processing.dto.response.ProcessedUserData;
import com.arte.processing.dto.response.UserJobComparison;
import com.arte.processing.processor.prompts.UserJobComparisonAssistant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserJobComparisonProcessor {

    private final ObjectMapper objectMapper;

    public UserJobComparison process(ProcessedUserData userData, ProcessedJobData jobData, OpenAiOfficialChatModel model) {
        try {
            UserJobComparisonAssistant assistant = AiServices.create(UserJobComparisonAssistant.class, model);

            log.info("Comparing user {} with job {}", userData.userId(), jobData.jobId());
            
            String candidateJson = serializeToJson(userData);
            String jobJson = serializeToJson(jobData);

        UserJobComparison result = assistant.compareUserAndJob(
                    candidateJson,
                    jobJson
            );

            log.info("Successfully compared user {} with job {}", userData.userId(), jobData.jobId());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error comparing user {} with job {}", userData.userId(), jobData.jobId(), e);
            throw new RuntimeException("Failed to compare user and job", e);
        }
    }

    private String serializeToJson(Object obj) {
        if (obj == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object: {}", obj.getClass().getSimpleName(), e);
            return "{}";
        }
    }

}
