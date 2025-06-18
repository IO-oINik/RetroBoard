package ru.edu.retro.dbservice.models.dto.kafka;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BoardDtoKafka (
        UUID id,
        String title,
        Long userId,
        Boolean isProgress,
        LocalDateTime createdAt,
        LocalDateTime endedAt,
        UUID inviteEditToken,
        List<Long> editorsId
){}
