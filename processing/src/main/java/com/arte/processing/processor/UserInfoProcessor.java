package com.arte.processing.processor;

import com.arte.processing.entity.UserInfo;
import com.arte.processing.entity.UserKnowledgeBase;
import com.arte.processing.entity.Users;
import com.arte.processing.processor.prompts.UserInfoAssistant;
import com.arte.processing.dto.response.ProcessedUserData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserInfoProcessor {

    private final ObjectMapper objectMapper;

    public ProcessedUserData process(Users user, UserInfo userInfo, List<UserKnowledgeBase> knowledgeBase, OpenAiOfficialChatModel model) {
        try {
            UserInfoAssistant assistant = AiServices.create(UserInfoAssistant.class, model);

            String githubData = serializeToJson(userInfo.getGithubStats());
            String leetcodeData = serializeToJson(userInfo.getLeetcodeStats());
            String resumeData = serializeToJson(userInfo.getResumeSummary());
            String knowledgeBaseData = formatKnowledgeBase(knowledgeBase);

            log.info("Processing user info for user: {}", user.getId());
            
            ProcessedUserData result = assistant.analyzeUser(
                    String.valueOf(user.getId()),
                    githubData,
                    leetcodeData,
                    resumeData,
                    knowledgeBaseData
            );
            log.info("Result for the processed user data: {}", serializeToJson(result));

            log.info("Successfully processed user info for: {}", user.getId());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error processing user info for user: {}", user.getId(), e);
            throw new RuntimeException("Failed to process user info", e);
        }
    }

    private String serializeToJson(Object obj) {
        if (obj == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object: {}", obj.getClass().getSimpleName(), e);
            return "{}";
        }
    }

    private String formatKnowledgeBase(List<UserKnowledgeBase> knowledgeBase) {
        if (knowledgeBase == null || knowledgeBase.isEmpty()) {
            return "No additional knowledge base entries";
        }
        
        return knowledgeBase.stream()
                .map(kb -> String.format("Source: %s (%s)\nContent: %s\nMetadata: %s",
                        kb.getSourceType(),
                        kb.getSourceUrl(),
                        kb.getContent(),
                        serializeToJson(kb.getMetadata())))
                .collect(Collectors.joining("\n\n---\n\n"));
    }

}
