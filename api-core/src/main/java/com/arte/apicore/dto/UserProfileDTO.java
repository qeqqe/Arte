package com.arte.apicore.dto;

import com.arte.apicore.dto.github.GitHubStats;
import com.arte.apicore.dto.leetcode.LeetCodeStats;
import com.arte.apicore.dto.resume.ResumeSummary;

import java.time.Instant;

public record UserProfileDTO(
        String userId,
        String email,
        String githubUsername,
        GitHubStats githubStats,
        LeetCodeStats leetcodeStats,
        ResumeSummary resumeSummary,
        boolean hasProcessedData,
        Instant processedAt,
        Instant lastIngestedAt
) {}
