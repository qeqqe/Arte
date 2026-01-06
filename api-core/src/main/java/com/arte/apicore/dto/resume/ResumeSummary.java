package com.arte.apicore.dto.resume;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Jacksonized
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
