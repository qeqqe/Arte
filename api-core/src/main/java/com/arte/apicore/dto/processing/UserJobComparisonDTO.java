package com.arte.apicore.dto.processing;

import java.util.List;

public record UserJobComparisonDTO(
        String userId,
        String jobId,
        int overallMatchScore,
        int skillsMatchScore,
        int experienceMatchScore,
        int educationMatchScore,
        List<SkillGapDTO> skillGaps,
        List<String> strengths,
        List<String> recommendations,
        String fitAssessment
) {}
