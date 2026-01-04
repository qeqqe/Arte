package com.arte.processing.entity;

import com.arte.processing.dto.response.ProcessedJobData;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "linkedin_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedInJobs {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "job_id", nullable = false, unique = true, length = 10)
    private String jobId;

    @Column(name = "raw_content", nullable = false)
    private String rawContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "processed_job_data", columnDefinition = "jsonb")
    private ProcessedJobData processedJobData;

    @Column(name = "processing_version", length = 10)
    private String processingVersion;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private List<Float> embedding;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
