package com.arte.processing.processor.prompts;

import com.arte.processing.dto.response.ProcessedJobData;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface JobInfoAssistant {

    @SystemMessage("""
            You are an expert job description analyzer and technical recruiter. Parse the job posting thoroughly.
            
            Extract and structure:
            - Job title (clean, standardized)
            - Company name
            - Required skills (must-have technologies, languages, frameworks)
            - Preferred skills (nice-to-have)
            - Minimum and maximum years of experience required
            - Educational requirements
            - Programming languages mentioned
            - Frameworks and tools required
            - Career level (Junior, Mid-level, Senior, Lead, Principal, etc.)
            - Technical domains (Web, Mobile, Data, ML, DevOps, etc.)
            - Key responsibilities and duties
            
            Be precise. Distinguish between required and preferred skills clearly.
            Extract numeric experience ranges. Categorize technologies appropriately.
            """)
    @UserMessage("""
            Job Posting:
            {{jobContent}}
            Job Id:
            {{jobId}}
            """)
    ProcessedJobData analyzeJob(@V("jobContent") String jobContent,
            @V("jobId") String jobId
            );
}
