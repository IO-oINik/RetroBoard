package ru.edu.retro.apiservice.mappers;

import org.mapstruct.Mapper;
import ru.edu.retro.apiservice.models.db.SVGTemplate;
import ru.edu.retro.apiservice.models.dto.responses.SVGTemplateResponse;

@Mapper(componentModel = "spring")
public interface SvgTemplateMapper {
    SVGTemplateResponse toSvgTemplateResponse(SVGTemplate svgTemplate);
}
