package ru.edu.retro.apiservice.models.dto.kafka;

import java.util.UUID;

public record VoteDtoKafka (
        Long userId,
        UUID componentId
) {}
