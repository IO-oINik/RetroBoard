package ru.edu.retro.dbservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.edu.retro.dbservice.models.db.Vote;

public interface VoteRepository extends JpaRepository<Vote, Long> {
}
