package com.arte.processing.repository;

import com.arte.processing.entity.UserKnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserKnowledgeBaseRepository extends JpaRepository<UserKnowledgeBase, UUID> {

    List<UserKnowledgeBase> findByUserId(UUID userId);

    Optional<UserKnowledgeBase> findByUserIdAndSourceType(UUID userId, String sourceType);

    Optional<UserKnowledgeBase> findByUserIdAndSourceTypeAndSourceUrl(UUID userId, String sourceType, String sourceUrl);

    @Modifying
    @Query("DELETE FROM UserKnowledgeBase ukb WHERE ukb.user.id = :userId AND ukb.sourceType = :sourceType")
    void deleteByUserIdAndSourceType(@Param("userId") UUID userId, @Param("sourceType") String sourceType);

    @Query("SELECT ukb FROM UserKnowledgeBase ukb WHERE ukb.user.id = :userId AND ukb.sourceType IN :sourceTypes")
    List<UserKnowledgeBase> findByUserIdAndSourceTypes(@Param("userId") UUID userId, @Param("sourceTypes") List<String> sourceTypes);

    boolean existsByUserIdAndSourceTypeAndSourceUrl(UUID userId, String sourceType, String sourceUrl);
}
