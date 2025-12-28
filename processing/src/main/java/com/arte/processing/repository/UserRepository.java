package com.arte.processing.repository;

import com.arte.processing.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByGithubUsername(String githubUsername);

    Optional<Users> findByEmail(String email);

    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.userInfo WHERE u.id = :userId")
    Optional<Users> findByIdWithUserInfo(@Param("userId") UUID userId);

    boolean existsByGithubUsername(String githubUsername);

    boolean existsByEmail(String email);

}
