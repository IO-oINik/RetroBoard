package ru.edu.retro.dbservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.retro.dbservice.models.db.SVGTemplate;

@Repository
public interface SVGTemplateRepository extends JpaRepository<SVGTemplate, Long> {
}
