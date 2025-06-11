package ru.edu.retro.apiservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.edu.retro.apiservice.mappers.SvgTemplateMapper;
import ru.edu.retro.apiservice.models.dto.responses.SVGTemplateResponse;
import ru.edu.retro.apiservice.repositories.SVGTemplateRepositoryReadOnly;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SvgTemplateService {
    private final SVGTemplateRepositoryReadOnly repository;
    private final SvgTemplateMapper mapper;

    public List<SVGTemplateResponse> getAll() {
        log.debug("Fetching all SVG templates from repository");
        var templates = repository.findAll().stream()
                .map(mapper::toSvgTemplateResponse)
                .toList();
        log.debug("Fetched {} SVG templates", templates.size());
        return templates;
    }
}
