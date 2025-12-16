package com.arte.ingestion.service;

import com.arte.ingestion.dto.resume.ResumeSummary;
import com.arte.ingestion.entity.UserInfo;
import com.arte.ingestion.entity.UserKnowledgeBase;
import com.arte.ingestion.entity.Users;
import com.arte.ingestion.repository.UserInfoRepository;
import com.arte.ingestion.repository.UserKnowledgeBaseRepository;
import com.arte.ingestion.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; 

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeProcessingService {

    private static final String SOURCE_TYPE = "resume";
    private static final int DEFAULT_WORD_CAP = 3000;

    @Value("${ingestion.resume.word-cap:" + DEFAULT_WORD_CAP + "}")
    private int wordCap;

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserKnowledgeBaseRepository knowledgeBaseRepository;
    private final ObjectMapper objectMapper;

    /**
     * processes a resume PDF, extracts text with word cap.
     *
     * @param userId The user's UUID
     * @param file   The uploaded PDF file
     * @return ProcessingResult with details of what was processed
     */
    @Transactional
    public ProcessingResult processResume(UUID userId, MultipartFile file) throws IOException {
        log.info("Starting resume processing for user: {}, file: {}", userId, file.getOriginalFilename());

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 1. validate file
        if (!isPdfFile(file)) {
            return new ProcessingResult(false, "Invalid file type. Only PDF files are supported.", 0);
        }

        // 2. extract text from PDF
        String rawText = extractTextFromPdf(file.getInputStream());
        if (rawText == null || rawText.isBlank()) {
            return new ProcessingResult(false, "Could not extract text from PDF", 0);
        }

        // 3. clean and cap the text
        String cleanedText = cleanText(rawText);
        String cappedText = capWords(cleanedText, wordCap);
        int wordCount = countWords(cappedText);

        log.info("Extracted {} words from resume (capped at {})", wordCount, wordCap);

        // 4. compute file hash for deduplication
        String fileHash = computeHash(file.getBytes());

        // 5. build resume summary
        ResumeSummary summary = ResumeSummary.builder()
                .fileName(file.getOriginalFilename())
                .fileHash(fileHash)
                .wordCount(wordCount)
                .processedAt(Instant.now())
                .rawText(cappedText)
                .skills(extractSkills(cappedText))
                .experiences(extractExperiences(cappedText))
                .education(extractEducation(cappedText))
                .summary(extractSummary(cappedText))
                .build();

        // 6. update user_info with resume summary
        UserInfo userInfo = userInfoRepository.findById(userId)
                .orElse(UserInfo.builder()
                        .user(user)
                        .build());

        userInfo.setResumeSummary(objectMapper.convertValue(summary, Map.class));
        userInfo.setLastIngestedAt(Instant.now());
        userInfoRepository.save(userInfo);

        // 7. create knowledge base entry
        String sourceUrl = "resume://" + userId + "/" + fileHash;
        Map<String, Object> metadata = Map.of(
                "fileName", file.getOriginalFilename(),
                "fileHash", fileHash,
                "wordCount", wordCount,
                "processedAt", Instant.now().toString()
        );

        UserKnowledgeBase entry = knowledgeBaseRepository
                .findByUserIdAndSourceTypeAndSourceUrl(userId, SOURCE_TYPE, sourceUrl)
                .map(existing -> {
                    existing.setContent(cappedText);
                    existing.setMetadata(metadata);
                    return existing;
                })
                .orElse(UserKnowledgeBase.builder()
                        .user(user)
                        .content(cappedText)
                        .sourceType(SOURCE_TYPE)
                        .sourceUrl(sourceUrl)
                        .metadata(metadata)
                        .build());

        knowledgeBaseRepository.save(entry);

        log.info("Resume processing completed for user {}: {} words", userId, wordCount);

        return new ProcessingResult(true, "Successfully processed resume", wordCount);
    }

    private boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        return "application/pdf".equals(contentType) ||
               (fileName != null && fileName.toLowerCase().endsWith(".pdf"));
    }

    private String extractTextFromPdf(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("Failed to extract text from PDF", e);
            return null;
        }
    }

    private String cleanText(String text) {
        // rm -rf excessive whitespace
        text = text.replaceAll("\\s+", " ");
        // rm -rf special characters but keep meaningful punctuation
        text = text.replaceAll("[^\\p{L}\\p{N}\\p{P}\\s]", "");
        // normalize line endings
        text = text.replaceAll("\\r\\n|\\r", "\n");
        // rm -rf multiple newlines
        text = text.replaceAll("\\n{3,}", "\n\n");
        return text.trim();
    }

    private String capWords(String text, int maxWords) {
        String[] words = text.split("\\s+");
        if (words.length <= maxWords) {
            return text;
        }
        return String.join(" ", Arrays.copyOfRange(words, 0, maxWords));
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.split("\\s+").length;
    }

    private String computeHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 16); // first 16 chars
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString().substring(0, 16);
        }
    }

    // extracts skills from resume text using pattern matching
    private List<String> extractSkills(String text) {
        List<String> skills = new ArrayList<>();
        
        // technical skills patterns
        String[] skillPatterns = {
            "(?i)skills?[:\\s]+([^\\n]+)",
            "(?i)technical\\s+skills?[:\\s]+([^\\n]+)",
            "(?i)programming\\s+languages?[:\\s]+([^\\n]+)",
            "(?i)technologies?[:\\s]+([^\\n]+)",
            "(?i)frameworks?[:\\s]+([^\\n]+)"
        };
        
        for (String pattern : skillPatterns) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(text);
            while (m.find()) {
                String skillLine = m.group(1).trim();
                // split by common delimiters
                String[] skillList = skillLine.split("[,;|•·]");
                for (String skill : skillList) {
                    String cleaned = skill.trim();
                    if (!cleaned.isEmpty() && cleaned.length() < 50) {
                        skills.add(cleaned);
                    }
                }
            }
        }
        
        return skills.stream().distinct().limit(30).toList();
    }

    // extracts work experience sections from resume text
    private List<String> extractExperiences(String text) {
        List<String> experiences = new ArrayList<>();
        
        Pattern expPattern = Pattern.compile(
            "(?i)(experience|employment|work\\s+history)[:\\s]*([\\s\\S]*?)(?=education|skills|projects|certifications|$)",
            Pattern.MULTILINE
        );
        
        Matcher m = expPattern.matcher(text);
        if (m.find()) {
            String expSection = m.group(2).trim();
            // split by common job entry patterns (dates, company names)
            String[] entries = expSection.split("(?i)(?=\\d{4}\\s*[-–]|january|february|march|april|may|june|july|august|september|october|november|december)");
            for (String entry : entries) {
                String cleaned = entry.trim();
                if (!cleaned.isEmpty() && cleaned.length() > 20) {
                    experiences.add(cleaned.length() > 500 ? cleaned.substring(0, 500) + "..." : cleaned);
                }
            }
        }
        
        return experiences.stream().limit(10).toList();
    }


    // extracts education section
    private List<String> extractEducation(String text) {
        List<String> education = new ArrayList<>();
        
        Pattern eduPattern = Pattern.compile(
            "(?i)(education|academic|qualifications?)[:\\s]*([\\s\\S]*?)(?=experience|skills|projects|certifications|$)",
            Pattern.MULTILINE
        );
        
        Matcher m = eduPattern.matcher(text);
        if (m.find()) {
            String eduSection = m.group(2).trim();
            // common patterns
            String[] entries = eduSection.split("(?i)(?=bachelor|master|phd|b\\.s\\.|m\\.s\\.|b\\.a\\.|m\\.a\\.|university|college|institute)");
            for (String entry : entries) {
                String cleaned = entry.trim();
                if (!cleaned.isEmpty() && cleaned.length() > 10) {
                    education.add(cleaned.length() > 300 ? cleaned.substring(0, 300) + "..." : cleaned);
                }
            }
        }
        
        return education.stream().limit(5).toList();
    }

    // extracts summary section
    private String extractSummary(String text) {
        Pattern summaryPattern = Pattern.compile(
            "(?i)(summary|objective|profile|about)[:\\s]*([^\\n]{50,500})",
            Pattern.MULTILINE
        );
        
        Matcher m = summaryPattern.matcher(text);
        if (m.find()) {
            return m.group(2).trim();
        }
        
        // fallback: take first meaningful paragraph
        String[] paragraphs = text.split("\\n\\n");
        for (String para : paragraphs) {
            if (para.length() > 100 && para.length() < 500) {
                return para.trim();
            }
        }
        
        return "";
    }

    public record ProcessingResult(
            boolean success,
            String message,
            int wordCount
    ) {}
}
