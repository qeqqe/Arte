package com.arte.processing.service;

import com.arte.processing.dto.response.ProcessedLLMResponse;
import com.arte.processing.dto.response.ProcessedUserData;
import com.arte.processing.dto.resume.ResumeSummary;
import com.arte.processing.entity.UserInfo;
import com.arte.processing.entity.UserKnowledgeBase;
import com.arte.processing.entity.Users;
import com.arte.processing.exception.UserNotFoundException;
import com.arte.processing.processor.UserInfoProcessor;
import com.arte.processing.provider.LLMProvider;
import com.arte.processing.repository.UserInfoRepository;
import com.arte.processing.repository.UserKnowledgeBaseRepository;
import com.arte.processing.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInfoProcessingService {

    private static final String DEFAULT_VERSION = "v1";

    private final LLMProvider llmProvider;
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserKnowledgeBaseRepository userKnowledgeBaseRepository;
    private final UserInfoProcessor userInfoProcessor;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProcessedLLMResponse processUserInfo(Users user) {
        UUID userId = user.getId();
        log.info("Starting user info processing for user: {}", userId);

        UserInfo userInfo = user.getUserInfo();
        String processingVersion  = userInfo.getProcessingVersion();
        processingVersion = processingVersion == null ? DEFAULT_VERSION : "v" + (Integer.parseInt(processingVersion.substring(1)) + 1);

        List<UserKnowledgeBase> knowledgeBase = userKnowledgeBaseRepository
                .findByUserIdAndSourceTypes(userId, List.of("github", "leetcode", "resume"));

        var model = llmProvider.getChatModel(user.getGithubToken());

        ProcessedLLMResponse result = userInfoProcessor.process(user, userInfo, knowledgeBase, model);

        persistProcessedUserData(userInfo, result, processingVersion);

        log.info("Successfully processed user info for user: {}", userId);
        return result;
    }

    @Transactional
    public ProcessedLLMResponse processUserInfoId(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        return processUserInfo(user);
    }


        private void persistProcessedUserData(UserInfo userInfo, ProcessedLLMResponse data, String version) {
        ResumeSummary resumeSummary = userInfo.getResumeSummary();

        resumeSummary.setSkills(data.resumeSkills());
        resumeSummary.setExperiences(data.resumeExperiences());
        resumeSummary.setEducation(data.education());
        resumeSummary.setSummary(data.resumeSummary());

        ProcessedUserData processedUserData = data.toUserData();

        userInfo.setResumeSummary(resumeSummary);
        userInfo.setProcessedUserData(processedUserData);
        userInfo.setProcessingVersion(version != null ? version : DEFAULT_VERSION);
        userInfo.setProcessedAt(Instant.now());
        userInfoRepository.save(userInfo);
        log.debug("Persisted processed user data for user: {}", userInfo.getUserId());
    }
}
