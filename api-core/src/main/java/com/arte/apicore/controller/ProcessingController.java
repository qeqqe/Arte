package com.arte.apicore.controller;

import com.arte.apicore.client.ProcessingServiceGrpcClient;
import com.arte.apicore.dto.processing.*;
import com.arte.apicore.grpc.processing.*;
import com.arte.apicore.service.auth.strategy.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/processing")
public class ProcessingController {

    private final ProcessingServiceGrpcClient processingClient;

    public ProcessingController(ProcessingServiceGrpcClient processingClient) {
        this.processingClient = processingClient;
    }

    @PostMapping("/user")
    public ResponseEntity<ProcessUserResponse> processUser(
            @RequestBody(required = false) ProcessUserRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        String version = request != null ? request.processingVersion() : "v1";
        ProcessUserInfoResponse response = processingClient.processUserInfo(
                UUID.fromString(user.userId()), version
        );

        return ResponseEntity.ok(new ProcessUserResponse(
                response.getSuccess(),
                response.getMessage(),
                toDTO(response.getProcessedData())
        ));
    }

    @PostMapping("/job")
    public ResponseEntity<ProcessJobResponse> processJob(
            @RequestBody ProcessJobRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        ProcessJobInfoResponse response = processingClient.processJobInfo(
                UUID.fromString(user.userId()), request.jobId()
        );

        return ResponseEntity.ok(new ProcessJobResponse(
                response.getSuccess(),
                response.getMessage(),
                toDTO(response.getProcessedData())
        ));
    }

    @PostMapping("/compare")
    public ResponseEntity<CompareUserJobResponse> compareUserJob(
            @RequestBody CompareUserJobRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        ProcessUserAndJobResponse response = processingClient.compareUserAndJob(
                UUID.fromString(user.userId()), request.jobId()
        );

        return ResponseEntity.ok(new CompareUserJobResponse(
                response.getSuccess(),
                response.getMessage(),
                toDTO(response.getComparison())
        ));
    }

    private ProcessedUserDataDTO toDTO(ProcessedUserData proto) {
        if (proto == null || proto.getUserId().isEmpty()) return null;

        return new ProcessedUserDataDTO(
                proto.getUserId(),
                proto.getTechnicalSkillsList(),
                proto.getSoftSkillsList(),
                proto.getWorkExperiencesList().stream().map(this::toDTO).toList(),
                proto.getCertificationsList(),
                proto.getEducationList(),
                proto.getYearsOfExperience(),
                proto.getProgrammingLanguagesList(),
                proto.getFrameworksList(),
                proto.getToolsList(),
                proto.getCareerLevel(),
                proto.getDomainsList()
        );
    }

    private WorkExperienceDTO toDTO(WorkExperience proto) {
        return new WorkExperienceDTO(
                proto.getCompany(),
                proto.getRole(),
                proto.getDuration(),
                proto.getTechnologiesList(),
                proto.getAchievementsList()
        );
    }

    private ProcessedJobDataDTO toDTO(ProcessedJobData proto) {
        if (proto == null || proto.getJobId().isEmpty()) return null;

        return new ProcessedJobDataDTO(
                proto.getJobId(),
                proto.getJobTitle(),
                proto.getCompany(),
                proto.getRequiredSkillsList(),
                proto.getPreferredSkillsList(),
                proto.getMinYearsExperience(),
                proto.getMaxYearsExperience(),
                proto.getRequiredEducationList(),
                proto.getProgrammingLanguagesList(),
                proto.getFrameworksList(),
                proto.getToolsList(),
                proto.getCareerLevel(),
                proto.getDomainsList(),
                proto.getResponsibilitiesList()
        );
    }

    private UserJobComparisonDTO toDTO(UserJobComparison proto) {
        if (proto == null || proto.getUserId().isEmpty()) return null;

        return new UserJobComparisonDTO(
                proto.getUserId(),
                proto.getJobId(),
                proto.getOverallMatchScore(),
                proto.getSkillsMatchScore(),
                proto.getExperienceMatchScore(),
                proto.getEducationMatchScore(),
                proto.getSkillGapsList().stream().map(this::toDTO).toList(),
                proto.getStrengthsList(),
                proto.getRecommendationsList(),
                proto.getFitAssessment()
        );
    }

    private SkillGapDTO toDTO(SkillGap proto) {
        return new SkillGapDTO(
                proto.getSkillName(),
                proto.getImportance(),
                proto.getSuggestion()
        );
    }
}
