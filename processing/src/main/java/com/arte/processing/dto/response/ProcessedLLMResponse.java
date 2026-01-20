package com.arte.processing.dto.response;

import java.util.List;

public record ProcessedLLMResponse(
        String userId,
        List<String> technicalSkills,
        List<String> softSkills,
        List<WorkExperience> workExperiences,
        List<String> certifications,
        List<String> education,
        int yearsOfExperience,
        List<String> programmingLanguages,
        List<String> frameworks,
        List<String> tools,
        String careerLevel,
        List<String> domains,
        // `resume_summary` update
        List<String> resumeSkills,
        List<String> resumeExperiences,
        List<String> resumeEducation,
        String resumeSummary
) {
    public ProcessedUserData toUserData() {
        return new ProcessedUserData(
                userId, technicalSkills, softSkills, workExperiences,
                certifications, education, yearsOfExperience,
                programmingLanguages, frameworks, tools, careerLevel, domains
        );
    }
}

