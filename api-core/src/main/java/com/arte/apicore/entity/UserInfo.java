package com.arte.apicore.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "user_info")
public class UserInfo {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne
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

    @Column(name = "last_ingested_at")
    private Instant lastIngestedAt;

    protected UserInfo() {}
}