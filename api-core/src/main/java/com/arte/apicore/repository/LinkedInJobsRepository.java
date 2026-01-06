package com.arte.apicore.repository;

import com.arte.apicore.entity.LinkedInJobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LinkedInJobsRepository extends JpaRepository<LinkedInJobs, UUID> {

    @Query("SELECT j FROM LinkedInJobs j WHERE j.jobId = :jobId")
    Optional<LinkedInJobs> findByJobId(@Param("jobId") String jobId);
}
