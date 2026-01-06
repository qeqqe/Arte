package com.arte.processing.dto.response;

import java.util.List;

public record UserJobComparison(
        String userId,
        String jobId,
        int overallMatchScore,
        int skillsMatchScore,
        int experienceMatchScore,
        int educationMatchScore,
        List<SkillGap> skillGaps,
        List<String> strengths,
        List<String> recommendations,
        String fitAssessment,
        String jobTitle,
        String company
) {}
