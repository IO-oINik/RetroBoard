package ru.edu.retro.apiservice.models.dto.requests;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.edu.retro.apiservice.models.db.ComponentType;

public record ComponentRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,
        String description,
        @NotNull(message = "Type cannot be null")
        ComponentType type,
        @DecimalMin(value = "0.0", message = "x => 0")
        @DecimalMax(value = "1.0", message = "x <= 1")
        @NotNull(message = "x cannot be null")
        Float x,
        @DecimalMin(value = "0.0", message = "y => 0")
        @DecimalMax(value = "1.0", message = "y <= 1")
        @NotNull(message = "y cannot be null")
        Float y,
        @NotNull(message = "isAnonymousAuthor cannot be null")
        Boolean isAnonymousAuthor,
        @NotNull(message = "isAnonymousVote cannot be null")
        Boolean isAnonymousVote,
        @NotNull(message = "sourceId cannot be null")
        Long sourceId
) {
}
