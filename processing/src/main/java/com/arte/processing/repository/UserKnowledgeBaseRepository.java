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

    @Query("SELECT ukb FROM UserKnowledgeBase ukb WHERE ukb.user.id = :userId")
    List<UserKnowledgeBase> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT ukb FROM UserKnowledgeBase ukb WHERE ukb.user.id = :userId AND ukb.sourceType = :sourceType")
    Optional<UserKnowledgeBase> findByUserIdAndSourceType(@Param("userId") UUID userId, @Param("sourceType") String sourceType);

    @Query("SELECT ukb FROM UserKnowledgeBase ukb WHERE ukb.user.id = :userId AND ukb.sourceType = :sourceType AND ukb.sourceUrl = :sourceUrl")
    Optional<UserKnowledgeBase> findByUserIdAndSourceTypeAndSourceUrl(@Param("userId") UUID userId, @Param("sourceType") String sourceType, @Param("sourceUrl") String sourceUrl);

    @Modifying
    @Query("DELETE FROM UserKnowledgeBase ukb WHERE ukb.user.id = :userId AND ukb.sourceType = :sourceType")
    void deleteByUserIdAndSourceType(@Param("userId") UUID userId, @Param("sourceType") String sourceType);

    @Query("SELECT ukb FROM UserKnowledgeBase ukb WHERE ukb.user.id = :userId AND ukb.sourceType IN :sourceTypes")
    List<UserKnowledgeBase> findByUserIdAndSourceTypes(@Param("userId") UUID userId, @Param("sourceTypes") List<String> sourceTypes);

    @Query("SELECT CASE WHEN COUNT(ukb) > 0 THEN true ELSE false END FROM UserKnowledgeBase ukb WHERE ukb.user.id = :userId AND ukb.sourceType = :sourceType AND ukb.sourceUrl = :sourceUrl")
    boolean existsByUserIdAndSourceTypeAndSourceUrl(@Param("userId") UUID userId, @Param("sourceType") String sourceType, @Param("sourceUrl") String sourceUrl);
}
