package ru.edu.retro.apiservice.models.dto.responses;

import java.util.UUID;

public record BoardInviteToken(
        UUID token
) {
}
