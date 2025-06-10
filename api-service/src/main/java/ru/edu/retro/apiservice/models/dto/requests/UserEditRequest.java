package ru.edu.retro.apiservice.models.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserEditRequest(
        @NotBlank(message = "Nickname cannot be blank")
        @Size(min = 5, max = 255,  message = "Nickname must be between 5 and 255 characters")
        String nickname
) {
}
