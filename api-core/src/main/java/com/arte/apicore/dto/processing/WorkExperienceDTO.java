package com.arte.apicore.dto.processing;

import java.util.List;

public record WorkExperienceDTO(
        String company,
        String role,
        String duration,
        List<String> technologies,
        List<String> achievements
) {}
