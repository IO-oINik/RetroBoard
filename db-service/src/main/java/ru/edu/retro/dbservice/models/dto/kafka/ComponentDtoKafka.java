package ru.edu.retro.dbservice.models.dto.kafka;

import ru.edu.retro.dbservice.models.db.ComponentType;

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
