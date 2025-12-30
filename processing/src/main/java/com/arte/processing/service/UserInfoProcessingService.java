package com.arte.processing.service;

import com.arte.processing.dto.response.ProcessedUserData;
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
    public ProcessedUserData processUserInfo(UUID userId) {
        log.info("Starting user info processing for user: {}", userId);

        Users user = userRepository.findByIdWithUserInfo(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        UserInfo userInfo = user.getUserInfo();
        String processingVersion  = userInfo.getProcessingVersion();
        processingVersion = processingVersion == null ? DEFAULT_VERSION : "v" + (Integer.parseInt(processingVersion.substring(1)) + 1);
        if (userInfo == null) {
            throw new IllegalStateException("No user info found for user: " + userId);
        }

        List<UserKnowledgeBase> knowledgeBase = userKnowledgeBaseRepository
                .findByUserIdAndSourceTypes(userId, List.of("github", "leetcode", "resume"));

        var model = llmProvider.getChatModel(user.getGithubToken());

        ProcessedUserData result = userInfoProcessor.process(user, userInfo, knowledgeBase, model);

        persistProcessedUserData(userInfo, result, processingVersion);

        log.info("Successfully processed user info for user: {}", userId);
        return result;
    }

    private void persistProcessedUserData(UserInfo userInfo, ProcessedUserData data, String version) {
        userInfo.setProcessedUserData(data);
        userInfo.setProcessingVersion(version != null ? version : DEFAULT_VERSION);
        userInfo.setProcessedAt(Instant.now());
        userInfoRepository.save(userInfo);
        log.debug("Persisted processed user data for user: {}", userInfo.getUserId());
    }
}
