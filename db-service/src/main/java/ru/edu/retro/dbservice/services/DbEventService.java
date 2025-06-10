package ru.edu.retro.dbservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.edu.retro.dbservice.models.db.Board;
import ru.edu.retro.dbservice.models.db.Component;
import ru.edu.retro.dbservice.models.db.Vote;
import ru.edu.retro.dbservice.models.dto.KafkaEvent;
import ru.edu.retro.dbservice.repositories.BoardRepository;
import ru.edu.retro.dbservice.repositories.ComponentRepository;
import ru.edu.retro.dbservice.repositories.VoteRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class DbEventService {
    private final BoardRepository boardRepository;
    private final ComponentRepository componentRepository;
    private final VoteRepository voteRepository;

    @KafkaListener(topics = "db-event", groupId = "db-event-group")
    public void listen(KafkaEvent<?> event) {
        log.info("Received event: action={}, entity={}", event.action(), event.entity());
        try {
            if ("CREATE".equals(event.action()) || "UPDATE".equals(event.action())) {
                switch (event.entity()) {
                    case "Board":
                        boardCreateOrUpdate((Board) event.payload());
                        break;
                    case "Component":
                        componentCreateOrUpdate((Component) event.payload());
                        break;
                    case "Vote":
                        voteCreateOrUpdate((Vote) event.payload());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown entity: " + event.entity() + " for " + event);
                }
            } else if ("DELETE".equals(event.action())) {
                switch (event.entity()) {
                    case "Board":
                        boardDelete((Board) event.payload());
                        break;
                    case "Component":
                        componentDelete((Component) event.payload());
                        break;
                    case "Vote":
                        voteDelete((Vote) event.payload());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown entity: " + event.entity() + " for " + event);
                }
            } else {
                throw new IllegalArgumentException("Unknown action: " + event.action() + " for " + event.entity());
            }
        } catch (Exception e) {
            log.error("Error processing event: action={}, entity={}, payload={}. Error: {}",
                    event.action(), event.entity(), event.payload(), e.getMessage(), e);
        }
    }

    private void boardCreateOrUpdate(Board board) {
        try {
            boardRepository.save(board);
            log.info("Board saved or updated: {}", board.getId());
        } catch (Exception e) {
            log.error("Failed to save or update Board: {}", board.getId(), e);
            throw e;
        }
    }

    private void boardDelete(Board board) {
        try {
            boardRepository.delete(board);
            log.info("Board deleted: {}", board.getId());
        } catch (Exception e) {
            log.error("Failed to delete Board: {}", board.getId(), e);
            throw e;
        }
    }

    private void componentCreateOrUpdate(Component component) {
        try {
            componentRepository.save(component);
            log.info("Component saved or updated: {}", component.getId());
        } catch (Exception e) {
            log.error("Failed to save or update Component: {}", component.getId(), e);
            throw e;
        }
    }

    private void componentDelete(Component component) {
        try {
            componentRepository.delete(component);
            log.info("Component deleted: {}", component.getId());
        } catch (Exception e) {
            log.error("Failed to delete Component: {}", component.getId(), e);
            throw e;
        }
    }

    private void voteCreateOrUpdate(Vote vote) {
        try {
            voteRepository.save(vote);
            log.info("Vote saved or updated: {}", vote.getId());
        } catch (Exception e) {
            log.error("Failed to save or update Vote: {}", vote.getId(), e);
            throw e;
        }
    }

    private void voteDelete(Vote vote) {
        try {
            voteRepository.delete(vote);
            log.info("Vote deleted: {}", vote.getId());
        } catch (Exception e) {
            log.error("Failed to delete Vote: {}", vote.getId(), e);
            throw e;
        }
    }
}

