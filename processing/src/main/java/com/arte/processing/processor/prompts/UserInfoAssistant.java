package com.arte.processing.processor.prompts;

import com.arte.processing.dto.response.ProcessedUserData;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface UserInfoAssistant {

    @SystemMessage("""
            You are an expert technical recruiter and career analyst. Analyze the provided user data comprehensively.
            
            Extract and structure the following information:
            - All technical skills (programming languages, frameworks, tools, technologies)
            - Soft skills (leadership, communication, problem-solving, etc.)
            - Work experiences with detailed breakdowns
            - Certifications and achievements
            - Educational background
            - Calculate years of experience based on work history and project timelines
            - Identify programming languages used (from GitHub, LeetCode, resume)
            - List frameworks and tools mentioned anywhere
            - Determine career level (Junior: 0-2 years, Mid-level: 2-5 years, Senior: 5-8 years, Lead: 8+ years)
            - Identify technical domains/specializations (e.g., Web Development, Machine Learning, DevOps)
            
            Be thorough and extract every relevant detail. Combine information from GitHub projects, LeetCode stats, and resume.
            Deduplicate skills across sources. Infer additional skills from project descriptions.
            """)
    @UserMessage("""
            User Id:
            {{userId}}
            GitHub Data:
            {{githubData}}
            
            LeetCode Data:
            {{leetcodeData}}
            
            Resume Data:
            {{resumeData}}
            
            Knowledge Base Entries:
            {{knowledgeBase}}
            """)
    ProcessedUserData analyzeUser(
            @V("userId") String userId,
            @V("githubData") String githubData,
            @V("leetcodeData") String leetcodeData,
            @V("resumeData") String resumeData,
            @V("knowledgeBase") String knowledgeBase
    );
}