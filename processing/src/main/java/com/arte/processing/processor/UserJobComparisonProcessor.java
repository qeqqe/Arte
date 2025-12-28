package com.arte.processing.processor;

import com.arte.processing.grpc.ProcessedJobData;
import com.arte.processing.grpc.ProcessedUserData;
import com.arte.processing.grpc.UserJobComparison;
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

            log.info("Comparing user {} with job {}", userData.getUserId(), jobData.getJobId());
            
            String candidateJson = serializeToJson(userData);
            String jobJson = serializeToJson(jobData);

            com.arte.processing.dto.response.UserJobComparison result = assistant.compareUserAndJob(
                    candidateJson,
                    jobJson
            );

            log.info("Successfully compared user {} with job {}", userData.getUserId(), jobData.getJobId());
            
            return convertToProto(result);
            
        } catch (Exception e) {
            log.error("Error comparing user {} with job {}", userData.getUserId(), jobData.getJobId(), e);
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

    private UserJobComparison convertToProto(com.arte.processing.dto.response.UserJobComparison dto) {
        UserJobComparison.Builder builder = UserJobComparison.newBuilder()
                .setUserId(dto.userId() != null ? dto.userId() : "")
                .setJobId(dto.jobId() != null ? dto.jobId() : "")
                .setOverallMatchScore(dto.overallMatchScore())
                .setSkillsMatchScore(dto.skillsMatchScore())
                .setExperienceMatchScore(dto.experienceMatchScore())
                .setEducationMatchScore(dto.educationMatchScore())
                .setFitAssessment(dto.fitAssessment() != null ? dto.fitAssessment() : "");

        if (dto.strengths() != null) {
            builder.addAllStrengths(dto.strengths());
        }
        if (dto.recommendations() != null) {
            builder.addAllRecommendations(dto.recommendations());
        }
        if (dto.skillGaps() != null) {
            dto.skillGaps().forEach(gap -> {
                com.arte.processing.grpc.SkillGap.Builder gapBuilder = 
                        com.arte.processing.grpc.SkillGap.newBuilder()
                        .setSkillName(gap.skillName() != null ? gap.skillName() : "")
                        .setImportance(gap.importance() != null ? gap.importance() : "")
                        .setSuggestion(gap.suggestion() != null ? gap.suggestion() : "");
                
                builder.addSkillGaps(gapBuilder.build());
            });
        }

        return builder.build();
    }
}
