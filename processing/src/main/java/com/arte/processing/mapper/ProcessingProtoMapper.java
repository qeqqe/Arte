package com.arte.processing.mapper;

import com.arte.processing.dto.response.ProcessedJobData;
import com.arte.processing.dto.response.ProcessedUserData;
import com.arte.processing.dto.response.SkillGap;
import com.arte.processing.dto.response.UserJobComparison;
import com.arte.processing.dto.response.WorkExperience;

public final class ProcessingProtoMapper {

    private ProcessingProtoMapper() {}

    public static com.arte.processing.grpc.ProcessedUserData toProto(ProcessedUserData dto) {
        if (dto == null) return com.arte.processing.grpc.ProcessedUserData.getDefaultInstance();

        var builder = com.arte.processing.grpc.ProcessedUserData.newBuilder()
                .setUserId(safe(dto.userId()))
                .setYearsOfExperience(dto.yearsOfExperience())
                .setCareerLevel(safe(dto.careerLevel()));

        if (dto.technicalSkills() != null) builder.addAllTechnicalSkills(dto.technicalSkills());
        if (dto.softSkills() != null) builder.addAllSoftSkills(dto.softSkills());
        if (dto.certifications() != null) builder.addAllCertifications(dto.certifications());
        if (dto.education() != null) builder.addAllEducation(dto.education());
        if (dto.programmingLanguages() != null) builder.addAllProgrammingLanguages(dto.programmingLanguages());
        if (dto.frameworks() != null) builder.addAllFrameworks(dto.frameworks());
        if (dto.tools() != null) builder.addAllTools(dto.tools());
        if (dto.domains() != null) builder.addAllDomains(dto.domains());

        if (dto.workExperiences() != null) {
            dto.workExperiences().forEach(exp -> builder.addWorkExperiences(toProto(exp)));
        }

        return builder.build();
    }

    public static com.arte.processing.grpc.WorkExperience toProto(WorkExperience dto) {
        if (dto == null) return com.arte.processing.grpc.WorkExperience.getDefaultInstance();

        var builder = com.arte.processing.grpc.WorkExperience.newBuilder()
                .setCompany(safe(dto.company()))
                .setRole(safe(dto.role()))
                .setDuration(safe(dto.duration()));

        if (dto.technologies() != null) builder.addAllTechnologies(dto.technologies());
        if (dto.achievements() != null) builder.addAllAchievements(dto.achievements());

        return builder.build();
    }

    public static com.arte.processing.grpc.ProcessedJobData toProto(ProcessedJobData dto) {
        if (dto == null) return com.arte.processing.grpc.ProcessedJobData.getDefaultInstance();

        var builder = com.arte.processing.grpc.ProcessedJobData.newBuilder()
                .setJobId(safe(dto.jobId()))
                .setJobTitle(safe(dto.jobTitle()))
                .setCompany(safe(dto.company()))
                .setMinYearsExperience(dto.minYearsExperience())
                .setMaxYearsExperience(dto.maxYearsExperience())
                .setCareerLevel(safe(dto.careerLevel()));

        if (dto.requiredSkills() != null) builder.addAllRequiredSkills(dto.requiredSkills());
        if (dto.preferredSkills() != null) builder.addAllPreferredSkills(dto.preferredSkills());
        if (dto.requiredEducation() != null) builder.addAllRequiredEducation(dto.requiredEducation());
        if (dto.programmingLanguages() != null) builder.addAllProgrammingLanguages(dto.programmingLanguages());
        if (dto.frameworks() != null) builder.addAllFrameworks(dto.frameworks());
        if (dto.tools() != null) builder.addAllTools(dto.tools());
        if (dto.domains() != null) builder.addAllDomains(dto.domains());
        if (dto.responsibilities() != null) builder.addAllResponsibilities(dto.responsibilities());

        return builder.build();
    }

    public static com.arte.processing.grpc.UserJobComparison toProto(UserJobComparison dto) {
        if (dto == null) return com.arte.processing.grpc.UserJobComparison.getDefaultInstance();

        var builder = com.arte.processing.grpc.UserJobComparison.newBuilder()
                .setUserId(safe(dto.userId()))
                .setJobId(safe(dto.jobId()))
                .setOverallMatchScore(dto.overallMatchScore())
                .setSkillsMatchScore(dto.skillsMatchScore())
                .setExperienceMatchScore(dto.experienceMatchScore())
                .setEducationMatchScore(dto.educationMatchScore())
                .setFitAssessment(safe(dto.fitAssessment()));

        if (dto.strengths() != null) builder.addAllStrengths(dto.strengths());
        if (dto.recommendations() != null) builder.addAllRecommendations(dto.recommendations());

        if (dto.skillGaps() != null) {
            dto.skillGaps().forEach(gap -> builder.addSkillGaps(toProto(gap)));
        }

        return builder.build();
    }

    public static com.arte.processing.grpc.SkillGap toProto(SkillGap dto) {
        if (dto == null) return com.arte.processing.grpc.SkillGap.getDefaultInstance();

        return com.arte.processing.grpc.SkillGap.newBuilder()
                .setSkillName(safe(dto.skillName()))
                .setImportance(safe(dto.importance()))
                .setSuggestion(safe(dto.suggestion()))
                .build();
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }
}
