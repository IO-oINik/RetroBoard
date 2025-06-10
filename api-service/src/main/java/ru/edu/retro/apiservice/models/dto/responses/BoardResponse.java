package ru.edu.retro.apiservice.models.dto.responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record BoardResponse(
        UUID id,
        String title,
        UserResponse author,
        Boolean isProgress,
        LocalDateTime createdAt,
        LocalDateTime endedAt
) {
}
