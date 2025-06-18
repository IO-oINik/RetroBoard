package ru.edu.retro.apiservice.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;
import ru.edu.retro.apiservice.models.db.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Repository
public interface ComponentRepositoryReadOnly extends Repository<Component, Long> {
    @EntityGraph(attributePaths = {"author", "votes"})
    List<Component> findComponentByBoardId(UUID id);
    @EntityGraph(attributePaths = {"author", "votes"})
    Optional<Component> findComponentById(UUID id);
}
