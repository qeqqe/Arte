package com.arte.ingestion.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "user_knowledge_base")
@Setter
@Getter
public class UserKnowledgeBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private Users users;

    @Column(name = "content")
    private String content;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private List<Float> vector;
}
