package com.arte.apicore.dto.processing;

import java.util.List;

public record ProcessedUserDataDTO(
        String userId,
        List<String> technicalSkills,
        List<String> softSkills,
        List<WorkExperienceDTO> workExperiences,
        List<String> certifications,
        List<String> education,
        int yearsOfExperience,
        List<String> programmingLanguages,
        List<String> frameworks,
        List<String> tools,
        String careerLevel,
        List<String> domains
) {}
