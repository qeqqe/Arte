package com.arte.ingestion.dto.github;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReadmeContent {
    private String repoUrl;
    private String content; // readme content
    private String format; // base64 encoded will be decoded to Markdown...
}
