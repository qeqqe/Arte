package com.arte.ingestion.service;


import com.arte.ingestion.entity.LinkedInJobs;
import com.arte.ingestion.repository.LinkedInJobsRepository;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class LinkedInJobIngestionService  {

    private final LinkedInJobsRepository linkedInJobsRepository;

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

        Optional<LinkedInJobs> existing = linkedInJobsRepository.findByJobId(jobId);

        if(existing.isPresent()) {
            LinkedInJobs job = existing.get();
            return new LinkedInIngestionResult(true, job.getRawContent());
        }

        String content = jobContent(jobId);

        if (content == null) {
            log.warn("Job or job content not found for the ID: {}", jobId);
            return new LinkedInIngestionResult(false, "Job or Job content not found for: {}" + jobId);
        }

        LinkedInJobs entry = LinkedInJobs.builder()
                .jobId(jobId)
                .rawContent(content)
                .build();
        linkedInJobsRepository.save(entry);

        return new LinkedInIngestionResult(true, content);
    }

    private String jobContent(String jobId) throws IOException {
        if (jobId.length() != 10 || !jobId.chars().allMatch(Character::isDigit)) {
            log.warn("Invalid JobId format (expected 10 digits).");
            return null;
        }

        Document doc = Jsoup.connect("https://www.linkedin.com/jobs/view/" + jobId)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
                .get();

        Element jobDataHtml = doc.selectFirst(".show-more-less-html__markup--clamp-after-5");

        if (jobDataHtml == null || !jobDataHtml.hasText()) {
            log.warn("Job data element not found.");
            return null;
        }

        // clean HTML before conversion
        cleanHtmlForMarkdown(jobDataHtml);

        return FlexmarkHtmlConverter.builder()
                .build()
                .convert(jobDataHtml.html());
    }

    private void cleanHtmlForMarkdown(Element element) {
        // remove line breaks after strong tags
        element.select("strong + br").remove();

        // convert strong tags that act as headers to proper h3
        element.select("strong").forEach(strong -> {
            String text = strong.text().trim();
            if (text.length() > 0 && !strong.parent().tagName().equals("li")) {
                strong.before("<h3>" + text + "</h3>");
                strong.remove();
            }
        });

        // remove redundant br tags
        element.select("br + br").remove();
    }

    public record LinkedInIngestionResult(
            boolean success,
            String message
    ) {}
}
