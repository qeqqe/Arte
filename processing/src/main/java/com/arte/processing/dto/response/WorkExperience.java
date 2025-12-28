package com.arte.processing.dto.response;

import java.util.List;

public record WorkExperience(
        String company,
        String role,
        String duration,
        List<String> technologies,
        List<String> achievements
) {}
