package ru.edu.retro.apiservice.models.dto.responses;

public record ErrorResponse(
        Integer status,
        String message
) {
}
