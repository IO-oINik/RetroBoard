package ru.edu.retro.apiservice.models.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank(message = "refreshToken cannot be blank")
        String refreshToken
) {
}
