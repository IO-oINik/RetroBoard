package ru.edu.retro.dbservice.models.dto.kafka;

import java.util.UUID;

public record VoteDtoKafka (
        Long userId,
        UUID componentId
) {}
