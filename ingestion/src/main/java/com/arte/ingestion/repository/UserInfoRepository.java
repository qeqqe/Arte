package com.arte.ingestion.repository;

import com.arte.ingestion.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {

    @Query("SELECT ui FROM UserInfo ui WHERE ui.lastIngestedAt < :threshold")
    List<UserInfo> findStaleUserInfo(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT ui FROM UserInfo ui WHERE SIZE(ui.githubStats) > 0")
    List<UserInfo> findUsersWithGitHubData();

    @Query(value = "SELECT * FROM user_info WHERE github_stats->'totalStars' > :minStars",
            nativeQuery = true)
    List<UserInfo> findByMinimumStars(@Param("minStars") int minStars);

}
