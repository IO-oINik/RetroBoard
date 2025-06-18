package ru.edu.retro.apiservice.models.dto.kafka;

import ru.edu.retro.apiservice.models.db.ComponentType;

import java.util.UUID;

public record ComponentDtoKafka(
        UUID id,
        String title,
        String description,
        ComponentType type,
        Float x,
        Float y,
        UUID boardId,
        Long authorId,
        Boolean isAnonymousAuthor,
        Boolean isAnonymousVotes
) {}
