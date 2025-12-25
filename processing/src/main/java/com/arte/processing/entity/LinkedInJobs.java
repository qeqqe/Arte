package com.arte.processing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
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

    @Column(name = "job_id", nullable = false, unique = true)
    private String jobId;

    @Column(name = "raw_content", nullable = false)
    private String rawContent;

    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private List<Float> embedding;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
