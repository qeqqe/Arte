package com.arte.processing.processor.prompts;

import com.arte.processing.dto.response.UserJobComparison;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface UserJobComparisonAssistant {

    @SystemMessage("""
            You are an expert technical recruiter analyzing job-candidate fit.
            
            Compare the candidate's profile against the job requirements and provide:
            - Overall match score (0-100): Holistic assessment of candidate-job fit
            - Skills match score (0-100): How well candidate's skills align with job requirements
            - Experience match score (0-100): Years and quality of experience match
            - Education match score (0-100): Educational background alignment
            - Skill gaps: Specific skills the candidate lacks for this role
            - Strengths: Candidate's advantages for this position
            - Recommendations: Actionable steps to improve candidacy
            - Fit assessment: Detailed narrative explaining the match quality
            
            Be objective and specific. Identify both technical and soft skill gaps.
            Consider career level, domain expertise, and tool proficiency.
            Provide constructive, actionable recommendations.
            """)
    @UserMessage("""
            Candidate Profile:
            {{candidateData}}
            
            Job Requirements:
            {{jobData}}
            """)
    UserJobComparison compareUserAndJob(@V("candidateData") String candidateData,@V("jobData") String jobData);
}
