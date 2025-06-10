package ru.edu.retro.apiservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.edu.retro.apiservice.mappers.SvgTemplateMapper;
import ru.edu.retro.apiservice.models.dto.responses.SVGTemplateResponse;
import ru.edu.retro.apiservice.repositories.SVGTemplateRepositoryReadOnly;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SvgTemplateService {
    private final SVGTemplateRepositoryReadOnly repository;
    private final SvgTemplateMapper mapper;

    public List<SVGTemplateResponse> getAll() {
        return repository.findAll().stream().map(mapper::toSvgTemplateResponse).toList();
    }
}
