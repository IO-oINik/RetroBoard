package ru.edu.retro.apiservice.models.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoardRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title
) {
}
