package com.arte.processing.repository;

import com.arte.processing.entity.UserJobComparisons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJobComparisonsRepository extends JpaRepository<UserJobComparisons, UUID> {

    @Query("SELECT ujc FROM UserJobComparisons ujc WHERE ujc.user.id = :userId AND ujc.jobId = :jobId")
    Optional<UserJobComparisons> findByUserIdAndJobId(@Param("userId") UUID userId, @Param("jobId") String jobId);

    @Query("SELECT ujc FROM UserJobComparisons ujc WHERE ujc.user.id = :userId ORDER BY ujc.createdAt DESC")
    List<UserJobComparisons> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT ujc FROM UserJobComparisons ujc WHERE ujc.user.id = :userId ORDER BY ujc.matchScore DESC")
    List<UserJobComparisons> findByUserIdOrderByMatchScore(@Param("userId") UUID userId);

    // if it exists with
    boolean existsByUserIdAndJobId(UUID userId, String jobId);
}
