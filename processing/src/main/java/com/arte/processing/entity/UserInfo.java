package com.arte.processing.entity;

import com.arte.processing.dto.response.ProcessedUserData;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.arte.processing.dto.github.GitHubStats;
import com.arte.processing.dto.leetcode.LeetCodeStats;
import com.arte.processing.dto.resume.ResumeSummary;

import java.time.Instant;
import java.util.UUID;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "user_info")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserInfo {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private Users user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "leetcode_stats", columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private LeetCodeStats leetcodeStats;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "github_stats", columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private GitHubStats githubStats;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resume_summary", columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private ResumeSummary resumeSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "processed_user_data", columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private ProcessedUserData processedUserData;

    @Column(name = "processing_version", length = 10)
    private String processingVersion;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "last_ingested_at")
    private Instant lastIngestedAt;
}