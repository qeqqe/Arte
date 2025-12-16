package com.arte.ingestion.service;

import com.arte.ingestion.client.LeetCodeGraphQLClient;
import com.arte.ingestion.entity.UserInfo;
import com.arte.ingestion.entity.UserKnowledgeBase;
import com.arte.ingestion.entity.Users;
import com.arte.ingestion.repository.UserInfoRepository;
import com.arte.ingestion.repository.UserKnowledgeBaseRepository;
import com.arte.ingestion.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeetCodeIngestionServiceTest {

    @Mock
    private LeetCodeGraphQLClient leetCodeClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserInfoRepository userInfoRepository;
    @Mock
    private UserKnowledgeBaseRepository knowledgeBaseRepository;

    private LeetCodeIngestionService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        service = new LeetCodeIngestionService(
                leetCodeClient,
                userRepository,
                userInfoRepository,
                knowledgeBaseRepository,
                objectMapper
        );
    }

    @Test
    void ingestLeetCodeData_userNotFound_throwsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ingestLeetCodeData(userId, "testuser"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void ingestLeetCodeData_leetcodeUserNotFound_returnsFailure() {
        UUID userId = UUID.randomUUID();
        Users user = createTestUser(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(leetCodeClient.fetchUserProfile(anyString())).thenReturn(null);

        var result = service.ingestLeetCodeData(userId, "nonexistent");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("not found");
    }

    @Test
    void ingestLeetCodeData_withValidProfile_processesSuccessfully() {
        UUID userId = UUID.randomUUID();
        Users user = createTestUser(userId);
        JsonNode profileData = createMockProfileResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(leetCodeClient.fetchUserProfile(anyString())).thenReturn(profileData);
        when(leetCodeClient.fetchRecentSubmissions(anyString(), anyInt())).thenReturn(createMockSubmissionsResponse());
        when(leetCodeClient.fetchContestRanking(anyString())).thenReturn(null);
        when(leetCodeClient.fetchLanguageStats(anyString())).thenReturn(null);
        when(userInfoRepository.findById(userId)).thenReturn(Optional.empty());
        when(userInfoRepository.save(any(UserInfo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(knowledgeBaseRepository.findByUserIdAndSourceTypeAndSourceUrl(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(knowledgeBaseRepository.save(any(UserKnowledgeBase.class)))
                .thenAnswer(inv -> {
                    UserKnowledgeBase kb = inv.getArgument(0);
                    kb.setId(UUID.randomUUID());
                    return kb;
                });

        var result = service.ingestLeetCodeData(userId, "testuser");

        assertThat(result.success()).isTrue();
        verify(userInfoRepository).save(any(UserInfo.class));
    }

    @Test
    void ingestLeetCodeData_withContestData_includesContestStats() {
        UUID userId = UUID.randomUUID();
        Users user = createTestUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(leetCodeClient.fetchUserProfile(anyString())).thenReturn(createMockProfileResponse());
        when(leetCodeClient.fetchRecentSubmissions(anyString(), anyInt())).thenReturn(null);
        when(leetCodeClient.fetchContestRanking(anyString())).thenReturn(createMockContestResponse());
        when(leetCodeClient.fetchLanguageStats(anyString())).thenReturn(null);
        when(userInfoRepository.findById(userId)).thenReturn(Optional.empty());
        when(userInfoRepository.save(any(UserInfo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(knowledgeBaseRepository.findByUserIdAndSourceTypeAndSourceUrl(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(knowledgeBaseRepository.save(any(UserKnowledgeBase.class)))
                .thenAnswer(inv -> {
                    UserKnowledgeBase kb = inv.getArgument(0);
                    kb.setId(UUID.randomUUID());
                    return kb;
                });

        var result = service.ingestLeetCodeData(userId, "testuser");

        assertThat(result.success()).isTrue();
    }

    private Users createTestUser(UUID userId) {
        Users user = new Users("test@test.com", "testuser", "ghp_testtoken");
        user.setId(userId);
        return user;
    }

    private JsonNode createMockProfileResponse() {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode data = root.putObject("data");
        ObjectNode matchedUser = data.putObject("matchedUser");
        matchedUser.put("username", "testuser");
        
        ObjectNode profile = matchedUser.putObject("profile");
        profile.put("ranking", 50000);
        profile.put("reputation", 100);
        profile.put("starRating", 4.5);
        profile.put("aboutMe", "Test user bio");

        ObjectNode submitStats = matchedUser.putObject("submitStatsGlobal");
        var acArray = submitStats.putArray("acSubmissionNum");
        
        ObjectNode all = acArray.addObject();
        all.put("difficulty", "All");
        all.put("count", 150);
        
        ObjectNode easy = acArray.addObject();
        easy.put("difficulty", "Easy");
        easy.put("count", 80);
        
        ObjectNode medium = acArray.addObject();
        medium.put("difficulty", "Medium");
        medium.put("count", 50);
        
        ObjectNode hard = acArray.addObject();
        hard.put("difficulty", "Hard");
        hard.put("count", 20);

        matchedUser.putArray("badges");
        matchedUser.putNull("activeBadge");

        return root;
    }

    private JsonNode createMockSubmissionsResponse() {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode data = root.putObject("data");
        var submissions = data.putArray("recentAcSubmissionList");
        
        ObjectNode sub1 = submissions.addObject();
        sub1.put("id", "12345");
        sub1.put("title", "Two Sum");
        sub1.put("titleSlug", "two-sum");
        sub1.put("timestamp", System.currentTimeMillis() / 1000);
        sub1.put("lang", "java");

        return root;
    }

    private JsonNode createMockContestResponse() {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode data = root.putObject("data");
        ObjectNode ranking = data.putObject("userContestRanking");
        ranking.put("attendedContestsCount", 10);
        ranking.put("rating", 1650.5);
        ranking.put("globalRanking", 25000);
        ranking.put("topPercentage", 15.5);
        
        data.putArray("userContestRankingHistory");
        
        return root;
    }
}
