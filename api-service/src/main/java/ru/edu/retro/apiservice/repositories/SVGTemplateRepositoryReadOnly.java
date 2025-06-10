package ru.edu.retro.apiservice.repositories;

import org.springframework.data.repository.Repository;
import ru.edu.retro.apiservice.models.db.SVGTemplate;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface SVGTemplateRepositoryReadOnly extends Repository<SVGTemplate, Long> {
    Optional<SVGTemplate> findById(Long id);
    List<SVGTemplate> findAll();
}
