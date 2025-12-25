package com.arte.processing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "github_username", nullable = false, unique = true)
    private String githubUsername;

    @Column(name = "github_token", nullable = false)
    private String githubToken;

    @Column(name = "leetcode_username")
    private String leetcodeUsername;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Users(String email, String githubUsername, String githubToken) {
        this.email = email;
        this.githubUsername = githubUsername;
        this.githubToken = githubToken;
    }
}

