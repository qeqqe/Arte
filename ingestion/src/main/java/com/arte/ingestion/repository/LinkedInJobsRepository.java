package com.arte.ingestion.repository;

import com.arte.ingestion.entity.LinkedInJobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LinkedInJobsRepository extends JpaRepository<LinkedInJobs,UUID> {

}
