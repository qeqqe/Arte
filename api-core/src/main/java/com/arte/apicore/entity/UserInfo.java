package com.arte.apicore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "user_info")
@Getter
@Setter
public class UserInfo {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private Users users;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "leetcode_stats", columnDefinition = "jsonb")
    private Map<String, Object> leetcodeStats;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "github_stats", columnDefinition = "jsonb")
    private Map<String, Object> githubStats;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resume_summary", columnDefinition = "jsonb")
    private Map<String, Object> resumeSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "processed_user_data", columnDefinition = "jsonb")
    private Map<String, Object> processedUserData;

    @Column(name = "processing_version", length = 10)
    private String processingVersion;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "last_ingested_at")
    private Instant lastIngestedAt;

    protected UserInfo() {}
}
