package com.arte.apicore.dto;

public record OnboardingStatusDTO(
        boolean hasGithubData,
        boolean hasLeetcodeData,
        boolean hasResumeData,
        boolean isOnboardingComplete
) {}
