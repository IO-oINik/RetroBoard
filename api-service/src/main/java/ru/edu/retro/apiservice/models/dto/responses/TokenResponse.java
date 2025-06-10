package ru.edu.retro.apiservice.models.dto.responses;

public record TokenResponse (
        String accessToken,
        String refreshToken,
        Long expiresAt
) {}
