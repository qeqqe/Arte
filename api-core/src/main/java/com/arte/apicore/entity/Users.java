package com.arte.apicore.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "users")
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
}

