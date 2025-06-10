package ru.edu.retro.apiservice.repositories;

import org.springframework.data.repository.Repository;
import ru.edu.retro.apiservice.models.db.Board;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Repository
public interface BoardRepositoryReadOnly extends Repository<Board, Long> {
    Optional<Board> findById(UUID id);
    List<Board> findBoardsByAuthorId(Long authorId);
}
