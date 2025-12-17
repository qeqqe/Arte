package com.arte.ingestion.service;


import com.arte.ingestion.entity.Users;
import com.arte.ingestion.repository.UserRepository;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class LinkedInJobIngestionService  {

    private final UserRepository userRepository;

    /**
     * Ingests LinkedIn jobs from the job id <a href="https://www.linkedin.com/jobs/view/">https://www.linkedin.com/jobs/view/{jobId}</a>
     * Scrapes the HTML class 'show-more-less-html__markup--clamp-after-5' with jsoup
     * and formats with flexmark.
     * @param userId user's UUID
     * @param jobId job's id
     * @return LinkedInIngestionResult with success and message
     */
    @Transactional
    public LinkedInIngestionResult ingestLinkedInJob(UUID userId,  String jobId) throws IOException {
        log.info("Starting Job ingestion for user: {}, on: {}", userId, jobId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: {}" + userId));

        String content = jobContent(jobId);

        if (content == null) {
            log.warn("Job or job content not found for the ID: {}", jobId);
            return new LinkedInIngestionResult(false, "Job or Job content not found for: {}" + jobId);
        }

        return new LinkedInIngestionResult(true, content);
    }

    private String jobContent(String jobId) throws IOException {
        if (jobId.length() != 10 && jobId.chars().allMatch(Character::isDigit)) {
            log.warn("Not a valid JobId (10 digit u_int only).");
            return null;
        }

        // get the raw HTML from the job
        Document doc = Jsoup.connect("https://www.linkedin.com/jobs/view/" + jobId)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
                .get();

        Document htmlString = Jsoup.parse(doc.html());

        // get the element
        Element jobDataHtml =  htmlString
                .selectFirst(".show-more-less-html__markup--clamp-after-5");

        if (jobDataHtml == null || !jobDataHtml.hasText()) {
            log.warn("Job data element not found.");
            return null;
        }

        // convert to Markdown
        return FlexmarkHtmlConverter.builder()
                .build()
                .convert(jobDataHtml.html());
    }

    public record LinkedInIngestionResult(
            boolean success,
            String message
    ) {}
}
