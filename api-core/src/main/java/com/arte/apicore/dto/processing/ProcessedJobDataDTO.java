package com.arte.apicore.dto.processing;

import java.util.List;

public record ProcessedJobDataDTO(
        String jobId,
        String jobTitle,
        String company,
        List<String> requiredSkills,
        List<String> preferredSkills,
        int minYearsExperience,
        int maxYearsExperience,
        List<String> requiredEducation,
        List<String> programmingLanguages,
        List<String> frameworks,
        List<String> tools,
        String careerLevel,
        List<String> domains,
        List<String> responsibilities
) {}
