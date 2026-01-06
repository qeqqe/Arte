package com.arte.apicore.controller;

import com.arte.apicore.dto.*;
import com.arte.apicore.entity.UserInfo;
import com.arte.apicore.entity.UserJobComparisons;
import com.arte.apicore.entity.Users;
import com.arte.apicore.repository.LinkedInJobsRepository;
import com.arte.apicore.repository.UserInfoRepository;
import com.arte.apicore.repository.UserJobComparisonsRepository;
import com.arte.apicore.service.auth.strategy.UserPrincipal;
import com.arte.apicore.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserInfoRepository userInfoRepository;
    private final UserJobComparisonsRepository userJobComparisonsRepository;
    private final LinkedInJobsRepository linkedInJobsRepository;

    public UserController(
            UserService userService,
            UserInfoRepository userInfoRepository,
            UserJobComparisonsRepository userJobComparisonsRepository,
            LinkedInJobsRepository linkedInJobsRepository) {
        this.userService = userService;
        this.userInfoRepository = userInfoRepository;
        this.userJobComparisonsRepository = userJobComparisonsRepository;
        this.linkedInJobsRepository = linkedInJobsRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(
            @AuthenticationPrincipal UserPrincipal user) {
        UUID userId = UUID.fromString(user.userId());
        Users userEntity = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<UserInfo> userInfo = userInfoRepository.findById(userId);

        return ResponseEntity.ok(new UserProfileDTO(
                userEntity.getId().toString(),
                userEntity.getEmail(),
                userEntity.getGithubUsername(),
                userInfo.map(UserInfo::getGithubStats).orElse(null),
                userInfo.map(UserInfo::getLeetcodeStats).orElse(null),
                userInfo.map(UserInfo::getResumeSummary).orElse(null),
                userInfo.map(ui -> ui.getProcessedUserData() != null).orElse(false),
                userInfo.map(UserInfo::getProcessedAt).orElse(null),
                userInfo.map(UserInfo::getLastIngestedAt).orElse(null)
        ));
    }

    @GetMapping("/onboarding-status")
    public ResponseEntity<OnboardingStatusDTO> getOnboardingStatus(
            @AuthenticationPrincipal UserPrincipal user) {
        UUID userId = UUID.fromString(user.userId());
        Optional<UserInfo> userInfo = userInfoRepository.findById(userId);

        boolean hasGithub = userInfo.map(ui -> ui.getGithubStats() != null).orElse(false);
        boolean hasLeetcode = userInfo.map(ui -> ui.getLeetcodeStats() != null).orElse(false);
        boolean hasResume = userInfo.map(ui -> ui.getResumeSummary() != null).orElse(false);

        return ResponseEntity.ok(new OnboardingStatusDTO(
                hasGithub,
                hasLeetcode,
                hasResume,
                hasGithub && hasLeetcode && hasResume
        ));
    }

    @GetMapping("/comparisons")
    public ResponseEntity<List<JobComparisonSummaryDTO>> getUserComparisons(
            @AuthenticationPrincipal UserPrincipal user) {
        UUID userId = UUID.fromString(user.userId());
        List<UserJobComparisons> comparisons = userJobComparisonsRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        List<JobComparisonSummaryDTO> summaries = comparisons.stream()
                .map(comp -> {
                    Map<String, Object> data = comp.getComparisonData();
                    String jobTitle = extractString(data, "jobTitle");
                    String company = extractString(data, "company");

                    return new JobComparisonSummaryDTO(
                            comp.getId().toString(),
                            comp.getJobId(),
                            jobTitle,
                            company,
                            comp.getMatchScore(),
                            comp.getCreatedAt()
                    );
                })
                .toList();

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/comparisons/{jobId}")
    public ResponseEntity<Map<String, Object>> getComparisonByJobId(
            @PathVariable String jobId,
            @AuthenticationPrincipal UserPrincipal user) {
        UUID userId = UUID.fromString(user.userId());
        return userJobComparisonsRepository.findByUserIdAndJobId(userId, jobId)
                .map(comp -> ResponseEntity.ok(comp.getComparisonData()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<LinkedInJobDTO> getJobByJobId(@PathVariable String jobId) {
        return linkedInJobsRepository.findByJobId(jobId)
                .map(job -> ResponseEntity.ok(new LinkedInJobDTO(
                        job.getId().toString(),
                        job.getJobId(),
                        job.getRawContent(),
                        job.getProcessedJobData(),
                        job.getProcessedJobData() != null,
                        job.getCreatedAt()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    private String extractString(Map<String, Object> data, String key) {
        if (data == null) return null;
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
}
