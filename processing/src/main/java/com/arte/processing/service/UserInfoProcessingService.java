package com.arte.processing.service;

import com.arte.processing.entity.UserKnowledgeBase;
import com.arte.processing.entity.Users;
import com.arte.processing.exception.UserNotFoundException;
import com.arte.processing.grpc.ProcessUserInfoRequest;
import com.arte.processing.grpc.ProcessedUserData;
import com.arte.processing.processor.UserInfoProcessor;
import com.arte.processing.provider.LLMProvider;
import com.arte.processing.repository.UserKnowledgeBaseRepository;
import com.arte.processing.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserInfoProcessingService {

    private final LLMProvider llmProvider;
    private final UserRepository userRepository;
    private final UserKnowledgeBaseRepository userKnowledgeBaseRepository;
    private final UserInfoProcessor userInfoProcessor;

    public UserInfoProcessingService(LLMProvider llmProvider, UserRepository userRepository, UserKnowledgeBaseRepository userKnowledgeBaseRepository, UserInfoProcessor userInfoProcessor) {
        this.llmProvider = llmProvider;
        this.userRepository = userRepository;
        this.userKnowledgeBaseRepository = userKnowledgeBaseRepository;
        this.userInfoProcessor = userInfoProcessor;
    }

    @Transactional
    public ProcessedUserData processUserInfo(ProcessUserInfoRequest request) throws IOException {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            log.info("Starting processing for the user: {}", userId);

            Users user = userRepository.findByIdWithUserInfo(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

            List<UserKnowledgeBase> userKnowledgeBase = userKnowledgeBaseRepository
                    .findByUserIdAndSourceTypes(userId, List.of("github", "leetcode", "resume"));

            var model = llmProvider.getChatModel(user.getGithubToken());

            return userInfoProcessor.process(user, user.getUserInfo(), userKnowledgeBase, model);

        } catch(Exception e) {
            log.error("Couldn't process the info for the user: {}", request.getUserId());
            throw new IOException(e);
        }
    }
}
