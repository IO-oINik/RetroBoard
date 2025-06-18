package ru.edu.retro.dbservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.edu.retro.dbservice.models.db.Vote;

import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    void deleteByUserIdAndComponentId(Long userId, UUID componentId);
}
