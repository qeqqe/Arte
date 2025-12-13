package com.arte.ingestion.dto.github;


import java.util.List;

public record RepositoryTopics(
        List<TopicWrapper> nodes
) {
    public record TopicWrapper(
            Topic topic
    ) {}

    public record Topic(
            String name
    ) {}
}