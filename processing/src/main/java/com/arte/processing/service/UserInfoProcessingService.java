package com.arte.processing.service;

import com.arte.processing.entity.Users;
import com.arte.processing.exception.UserNotFoundException;
import com.arte.processing.grpc.ProcessUserInfoResponse;
import com.arte.processing.provider.LLMProvider;
import com.arte.processing.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class UserInfoProcessingService {

    private final LLMProvider llmProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    public UserInfoProcessingService(LLMProvider llmProvider, UserRepository userRepository) {
        this.llmProvider = llmProvider;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProcessUserInfoResponse processUserInfo(UUID userId, String userInfoString) throws IOException {
        try {
            log.info("Starting processing for the user: {}", userId);

            JsonNode userInfo = objectMapper.readTree(userInfoString);
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

            OpenAiOfficialChatModel model = llmProvider.getChatModel(user.getGithubToken());

            return null;

        } catch(Exception e) {
            log.error("Couldn't process the info for the user: {}", userId);
            throw new IOException(e);
        }
    }
}
