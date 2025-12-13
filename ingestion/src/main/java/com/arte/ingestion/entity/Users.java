package com.arte.ingestion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "users")
@Getter
@Setter
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

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Users() {}

    public Users(String email, String githubUsername, String githubToken) {
        this.email = email;
        this.githubUsername = githubUsername;
        this.githubToken = githubToken;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGithubUsername() { return githubUsername; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }
    public String getGithubToken() { return githubToken; }
    public void setGithubToken(String githubToken) { this.githubToken = githubToken; }
    public Instant getCreatedAt() { return createdAt; }
}

