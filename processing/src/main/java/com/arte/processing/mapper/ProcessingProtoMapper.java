package com.arte.processing.mapper;

import com.arte.processing.grpc.*;
import java.util.List;
import java.util.Map;

public class ProcessingProtoMapper {

    @SuppressWarnings("unchecked")
    public static ProcessedUserData toProcessedUserData(Map<String, Object> data) {
        if (data == null) return ProcessedUserData.getDefaultInstance();

        var builder = ProcessedUserData.newBuilder()
                .setUserId(getString(data, "userId"))
                .setYearsOfExperience(getInt(data, "yearsOfExperience"))
                .setCareerLevel(getString(data, "careerLevel"));

        addListStrings(data, "technicalSkills", builder::addTechnicalSkills);
        addListStrings(data, "softSkills", builder::addSoftSkills);
        addListStrings(data, "certifications", builder::addCertifications);
        addListStrings(data, "education", builder::addEducation);
        addListStrings(data, "programmingLanguages", builder::addProgrammingLanguages);
        addListStrings(data, "frameworks", builder::addFrameworks);
        addListStrings(data, "tools", builder::addTools);
        addListStrings(data, "domains", builder::addDomains);

        Object workExp = data.get("workExperiences");
        if (workExp instanceof List) {
            ((List<Map<String, Object>>) workExp).forEach(exp ->
                    builder.addWorkExperiences(toWorkExperience(exp)));
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static WorkExperience toWorkExperience(Map<String, Object> data) {
        var builder = WorkExperience.newBuilder()
                .setCompany(getString(data, "company"))
                .setRole(getString(data, "role"))
                .setDuration(getString(data, "duration"));

        addListStrings(data, "achievements", builder::addAchievements);
        addListStrings(data, "technologies", builder::addTechnologies);

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    public static ProcessedJobData toProcessedJobData(Map<String, Object> data) {
        if (data == null) return ProcessedJobData.getDefaultInstance();

        var builder = ProcessedJobData.newBuilder()
                .setJobId(getString(data, "jobId"))
                .setJobTitle(getString(data, "jobTitle"))
                .setCompany(getString(data, "company"))
                .setMinYearsExperience(getInt(data, "minYearsExperience"))
                .setMaxYearsExperience(getInt(data, "maxYearsExperience"))
                .setCareerLevel(getString(data, "careerLevel"));

        addListStrings(data, "requiredSkills", builder::addRequiredSkills);
        addListStrings(data, "preferredSkills", builder::addPreferredSkills);
        addListStrings(data, "requiredEducation", builder::addRequiredEducation);
        addListStrings(data, "programmingLanguages", builder::addProgrammingLanguages);
        addListStrings(data, "frameworks", builder::addFrameworks);
        addListStrings(data, "tools", builder::addTools);
        addListStrings(data, "domains", builder::addDomains);
        addListStrings(data, "responsibilities", builder::addResponsibilities);

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    public static UserJobComparison toUserJobComparison(Map<String, Object> data) {
        if (data == null) return UserJobComparison.getDefaultInstance();

        var builder = UserJobComparison.newBuilder()
                .setUserId(getString(data, "userId"))
                .setJobId(getString(data, "jobId"))
                .setOverallMatchScore(getInt(data, "overallMatchScore"))
                .setSkillsMatchScore(getInt(data, "skillsMatchScore"))
                .setExperienceMatchScore(getInt(data, "experienceMatchScore"))
                .setEducationMatchScore(getInt(data, "educationMatchScore"))
                .setFitAssessment(getString(data, "fitAssessment"));

        addListStrings(data, "strengths", builder::addStrengths);
        addListStrings(data, "recommendations", builder::addRecommendations);

        Object gaps = data.get("skillGaps");
        if (gaps instanceof List) {
            ((List<Map<String, Object>>) gaps).forEach(gap ->
                    builder.addSkillGaps(toSkillGap(gap)));
        }

        return builder.build();
    }

    private static SkillGap toSkillGap(Map<String, Object> data) {
        return SkillGap.newBuilder()
                .setSkillName(getString(data, "skillName"))
                .setImportance(getString(data, "importance"))
                .setSuggestion(getString(data, "suggestion"))
                .build();
    }

    private static void addListStrings(Map<String, Object> data, String key, java.util.function.Consumer<String> adder) {
        Object list = data.get(key);
        if (list instanceof List) {
            ((List<?>) list).forEach(item -> {
                if (item != null) adder.accept(item.toString());
            });
        }
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private static int getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return (value instanceof Number) ? ((Number) value).intValue() : 0;
    }
}