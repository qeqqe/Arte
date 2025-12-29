package com.arte.processing.provider;

import com.openai.models.ChatModel;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
public class LLMProvider {
    public OpenAiOfficialChatModel getChatModel(String githubToken) {
        return OpenAiOfficialChatModel.builder()
                .apiKey(githubToken)
                .modelName(ChatModel.GPT_4_1_MINI)
                .supportedCapabilities(Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA))
                .strictJsonSchema(true)
                .isGitHubModels(true)
                .build();
    }
}
