package com.arte.ingestion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "user_info")
@Data
@Builder
@NoArgsConstructor
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
    private Map<String, Object> leetcodeStats;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "github_stats", columnDefinition = "jsonb")
    private Map<String, Object> githubStats;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resume_summary", columnDefinition = "jsonb")
    private Map<String, Object> resumeSummary;

    @Column(name = "last_ingested_at")
    private Instant lastIngestedAt;
}