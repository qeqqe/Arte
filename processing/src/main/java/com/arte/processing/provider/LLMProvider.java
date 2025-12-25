package com.arte.processing.provider;

import com.openai.models.ChatModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LLMProvider {
    public OpenAiOfficialChatModel getChatModel(String githubToken) {
        return OpenAiOfficialChatModel.builder()
                .apiKey(githubToken)
                .modelName(ChatModel.GPT_4_1_MINI)
                .isGitHubModels(true)
                .build();
    }
}
