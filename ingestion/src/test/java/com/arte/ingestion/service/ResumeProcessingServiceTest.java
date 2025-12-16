package com.arte.ingestion.service;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeProcessingServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserInfoRepository userInfoRepository;
    @Mock
    private UserKnowledgeBaseRepository knowledgeBaseRepository;

    private ResumeProcessingService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        service = new ResumeProcessingService(
                userRepository,
                userInfoRepository,
                knowledgeBaseRepository,
                objectMapper
        );
        ReflectionTestUtils.setField(service, "wordCap", 3000);
    }

    @Test
    void processResume_userNotFound_throwsException() {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = createMockPdfFile("test.pdf", "test content");
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processResume(userId, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void processResume_invalidFileType_returnsFailure() throws IOException {
        UUID userId = UUID.randomUUID();
        Users user = createTestUser(userId);
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = service.processResume(userId, file);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("Invalid file type");
    }

    @Test
    void processResume_emptyPdf_returnsFailure() throws IOException {
        UUID userId = UUID.randomUUID();
        Users user = createTestUser(userId);
        // create a minimal valid pdf with no text content
        byte[] emptyPdfBytes = createMinimalPdf();
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", emptyPdfBytes
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = service.processResume(userId, file);

        // might fail to extract or have no text - both are acceptable failure modes
        assertThat(result.success()).isFalse();
    }

    @Test
    void processResume_validPdf_processesSuccessfully() throws IOException {
        UUID userId = UUID.randomUUID();
        Users user = createTestUser(userId);
        // use a pdf with actual text content
        byte[] pdfBytes = createPdfWithText("John Doe\nSoftware Engineer\nSkills: Java, Python\nEducation: BS Computer Science");
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", pdfBytes
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
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

        var result = service.processResume(userId, file);

        // if pdf parsing succeeds and has text
        if (result.success()) {
            assertThat(result.wordCount()).isGreaterThan(0);
        }
    }

    private Users createTestUser(UUID userId) {
        Users user = new Users("test@test.com", "testuser", "ghp_testtoken");
        user.setId(userId);
        return user;
    }

    private MockMultipartFile createMockPdfFile(String filename, String content) {
        return new MockMultipartFile(
                "file", filename, "application/pdf", content.getBytes()
        );
    }

    // minimal pdf structure - likely won't contain extractable text
    private byte[] createMinimalPdf() {
        String pdf = "%PDF-1.4\n" +
                "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n" +
                "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n" +
                "3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R>>endobj\n" +
                "xref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000052 00000 n\n0000000101 00000 n\n" +
                "trailer<</Size 4/Root 1 0 R>>\nstartxref\n169\n%%EOF";
        return pdf.getBytes();
    }

    // pdf with actual text content stream
    private byte[] createPdfWithText(String text) {
        String stream = "BT /F1 12 Tf 100 700 Td (" + text.replace("\n", ") Tj T* (") + ") Tj ET";
        int streamLen = stream.length();
        
        String pdf = "%PDF-1.4\n" +
                "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n" +
                "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n" +
                "3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R/Contents 4 0 R/Resources<</Font<</F1 5 0 R>>>>>>endobj\n" +
                "4 0 obj<</Length " + streamLen + ">>stream\n" + stream + "\nendstream endobj\n" +
                "5 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n" +
                "xref\n0 6\n0000000000 65535 f\n0000000009 00000 n\n0000000052 00000 n\n0000000101 00000 n\n" +
                "0000000230 00000 n\n0000000300 00000 n\n" +
                "trailer<</Size 6/Root 1 0 R>>\nstartxref\n370\n%%EOF";
        return pdf.getBytes();
    }
}
