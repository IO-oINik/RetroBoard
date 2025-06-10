package ru.edu.retro.apiservice.models.dto.responses;

import ru.edu.retro.apiservice.models.db.ComponentType;

public record SVGTemplateResponse(
        Long id,
        ComponentType type,
        String source
) {
}
