package ru.edu.retro.apiservice.repositories;

import org.springframework.data.repository.Repository;
import ru.edu.retro.apiservice.models.db.Vote;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Repository
public interface VoteRepositoryReadOnly extends Repository<Vote, Long> {
    boolean existsByComponentIdAndUserId(UUID componentId, Long userId);
    Optional<Vote> findByComponentIdAndUserId(UUID componentId, Long userId);
    List<Vote> findAllByComponentId(UUID componentId);
}
