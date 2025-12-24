package com.arte.ingestion.service;

import com.arte.ingestion.client.GitHubGraphQLClient;
import com.arte.ingestion.dto.github.GitHubGraphQLResponse;
import com.arte.ingestion.dto.github.PrimaryLanguage;
import com.arte.ingestion.dto.github.RepositoryNode;
import com.arte.ingestion.dto.github.RepositoryTopics;
import com.arte.ingestion.entity.UserInfo;
import com.arte.ingestion.entity.UserKnowledgeBase;
import com.arte.ingestion.entity.Users;
import com.arte.ingestion.repository.UserInfoRepository;
import com.arte.ingestion.repository.UserKnowledgeBaseRepository;
import com.arte.ingestion.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubIngestionServiceTest {

    @Mock
    private GitHubGraphQLClient gitHubGraphQLClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserInfoRepository userInfoRepository;
    @Mock
    private UserKnowledgeBaseRepository knowledgeBaseRepository;

    private GitHubIngestionService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        service = new GitHubIngestionService(
                gitHubGraphQLClient,
                userRepository,
                userInfoRepository,
                knowledgeBaseRepository,
                objectMapper
        );
    }

    @Test
    void ingestGitHubData_userNotFound_throwsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ingestGitHubData(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void ingestGitHubData_noGitHubData_returnsFailure() {
        UUID userId = UUID.randomUUID();
        Users user = createTestUser(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(gitHubGraphQLClient.fetchPinnedRepos(anyString(), anyString())).thenReturn(null);

        var result = service.ingestGitHubData(userId);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("No GitHub data found");
    }

    @Test
    void ingestGitHubData_withPinnedRepos_processesSuccessfully() {
        UUID userId = UUID.randomUUID();
        Users user = createTestUser(userId);
        GitHubGraphQLResponse response = createMockGitHubResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(gitHubGraphQLClient.fetchPinnedRepos(anyString(), anyString())).thenReturn(response);
        when(gitHubGraphQLClient.fetchReadme(anyString(), anyString())).thenReturn("# Test README");
        when(knowledgeBaseRepository.findByUserIdAndSourceTypeAndSourceUrl(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(knowledgeBaseRepository.save(any(UserKnowledgeBase.class)))
                .thenAnswer(inv -> {
                    UserKnowledgeBase kb = inv.getArgument(0);
                    kb.setId(UUID.randomUUID());
                    return kb;
                });
        when(userInfoRepository.findById(userId)).thenReturn(Optional.empty());
        when(userInfoRepository.save(any(UserInfo.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.ingestGitHubData(userId);

        assertThat(result.success()).isTrue();
        assertThat(result.reposProcessed()).isEqualTo(1);
    }

    @Test
    void ingestGitHubData_updatesExistingKnowledgeBase() {
        UUID userId = UUID.randomUUID();
        UUID kbId = UUID.randomUUID();
        Users user = createTestUser(userId);
        GitHubGraphQLResponse response = createMockGitHubResponse();

        UserKnowledgeBase existingKb = UserKnowledgeBase.builder()
                .id(kbId)
                .content("old content")
                .sourceType("github")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(gitHubGraphQLClient.fetchPinnedRepos(anyString(), anyString())).thenReturn(response);
        when(gitHubGraphQLClient.fetchReadme(anyString(), anyString())).thenReturn("# Updated README");
        when(knowledgeBaseRepository.findByUserIdAndSourceTypeAndSourceUrl(any(), any(), any()))
                .thenReturn(Optional.of(existingKb));
        when(knowledgeBaseRepository.save(any(UserKnowledgeBase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userInfoRepository.findById(userId)).thenReturn(Optional.empty());
        when(userInfoRepository.save(any(UserInfo.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.ingestGitHubData(userId);

        assertThat(result.success()).isTrue();
        
        // verify entry was saved
        verify(knowledgeBaseRepository).save(any(UserKnowledgeBase.class));
    }

    private Users createTestUser(UUID userId) {
        Users user = new Users("test@test.com", "testuser", "ghp_testtoken");
        user.setId(userId);
        return user;
    }

    private GitHubGraphQLResponse createMockGitHubResponse() {
        RepositoryTopics.TopicWrapper topicWrapper = new RepositoryTopics.TopicWrapper(
                new RepositoryTopics.Topic("java")
        );
        
        RepositoryNode repo = new RepositoryNode(
                "test-repo",
                "A test repository",
                "https://github.com/testuser/test-repo",
                100,
                50,
                new PrimaryLanguage("Java", "#b07219"),
                new RepositoryTopics(List.of(topicWrapper))
        );

        return new GitHubGraphQLResponse(
                new GitHubGraphQLResponse.DataWrapper(
                        new GitHubGraphQLResponse.UserWrapper(
                                new GitHubGraphQLResponse.PinnedItems(List.of(repo))
                        )
                )
        );
    }
}
