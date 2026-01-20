package com.arte.processing.dto.response;

import java.util.List;

public record ProcessedUserData(
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
        List<String> domains
) {}