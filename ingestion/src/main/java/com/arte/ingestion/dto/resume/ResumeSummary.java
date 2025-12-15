package com.arte.ingestion.dto.resume;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ResumeSummary {
    private String fileName;
    private String fileHash;
    private Integer wordCount;
    private Instant processedAt;
    
    // extracted sections
    private String rawText;
    private List<String> skills;
    private List<String> experiences;
    private List<String> education;
    private String summary;
}
