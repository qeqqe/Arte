package com.arte.processing.processor;

import com.arte.processing.entity.LinkedInJobs;
import com.arte.processing.grpc.ProcessedJobData;
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
            
            com.arte.processing.dto.response.ProcessedJobData result = assistant.analyzeJob(job.getRawContent());

            log.info("Successfully processed job: {}", job.getJobId());
            
            return convertToProto(result);
            
        } catch (Exception e) {
            log.error("Error processing job: {}", job.getJobId(), e);
            throw new RuntimeException("Failed to process job", e);
        }
    }

    private ProcessedJobData convertToProto(com.arte.processing.dto.response.ProcessedJobData dto) {
        ProcessedJobData.Builder builder = ProcessedJobData.newBuilder()
                .setJobId(dto.jobId() != null ? dto.jobId() : "")
                .setJobTitle(dto.jobTitle() != null ? dto.jobTitle() : "")
                .setCompany(dto.company() != null ? dto.company() : "")
                .setMinYearsExperience(dto.minYearsExperience())
                .setMaxYearsExperience(dto.maxYearsExperience())
                .setCareerLevel(dto.careerLevel() != null ? dto.careerLevel() : "");

        if (dto.requiredSkills() != null) {
            builder.addAllRequiredSkills(dto.requiredSkills());
        }
        if (dto.preferredSkills() != null) {
            builder.addAllPreferredSkills(dto.preferredSkills());
        }
        if (dto.requiredEducation() != null) {
            builder.addAllRequiredEducation(dto.requiredEducation());
        }
        if (dto.programmingLanguages() != null) {
            builder.addAllProgrammingLanguages(dto.programmingLanguages());
        }
        if (dto.frameworks() != null) {
            builder.addAllFrameworks(dto.frameworks());
        }
        if (dto.tools() != null) {
            builder.addAllTools(dto.tools());
        }
        if (dto.domains() != null) {
            builder.addAllDomains(dto.domains());
        }
        if (dto.responsibilities() != null) {
            builder.addAllResponsibilities(dto.responsibilities());
        }

        return builder.build();
    }
}
