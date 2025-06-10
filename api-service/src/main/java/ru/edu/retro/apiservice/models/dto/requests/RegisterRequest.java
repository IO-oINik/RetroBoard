package ru.edu.retro.apiservice.models.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Nickname cannot be blank")
        @Size(min = 5, max = 255,  message = "Nickname must be between 5 and 255 characters")
        String nickname,
        @NotBlank(message = "Login cannot be blank")
        @Size(min = 5, max = 255, message = "Login must be between 5 and 255 characters")
        String login,
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 5, max = 255, message = "Password must be between 5 and 255 characters")
        String password
) {
}
