package ru.edu.retro.apiservice.models.dto.requests;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record ComponentEditRequest (
        String title,
        String description,
        @DecimalMin(value = "0.0", message = "x => 0")
        @DecimalMax(value = "1.0", message = "x <= 1")
        Float x,
        @DecimalMin(value = "0.0", message = "y => 0")
        @DecimalMax(value = "1.0", message = "y <= 1")
        Float y
) {
}
