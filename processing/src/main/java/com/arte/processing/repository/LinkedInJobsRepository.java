package com.arte.processing.repository;

import com.arte.processing.entity.LinkedInJobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LinkedInJobsRepository extends JpaRepository<LinkedInJobs,UUID> {
    Optional<LinkedInJobs> findByJobId(String id);
}
