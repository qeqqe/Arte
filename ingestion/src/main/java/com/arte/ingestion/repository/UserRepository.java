package com.arte.ingestion.repository;

import com.arte.ingestion.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByGithubUsername(String githubUsername);

    Optional<Users> findByEmail(String email);

    boolean existsByGithubUsername(String githubUsername);

    boolean existsByEmail(String email);

}
