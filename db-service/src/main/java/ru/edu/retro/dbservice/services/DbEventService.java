package ru.edu.retro.dbservice.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.edu.retro.dbservice.models.db.Board;
import ru.edu.retro.dbservice.models.db.Component;
import ru.edu.retro.dbservice.models.db.Vote;
import ru.edu.retro.dbservice.models.dto.KafkaEvent;
import ru.edu.retro.dbservice.models.dto.kafka.BoardDtoKafka;
import ru.edu.retro.dbservice.models.dto.kafka.ComponentDtoKafka;
import ru.edu.retro.dbservice.models.dto.kafka.VoteDtoKafka;
import ru.edu.retro.dbservice.repositories.BoardRepository;
import ru.edu.retro.dbservice.repositories.ComponentRepository;
import ru.edu.retro.dbservice.repositories.UserRepository;
import ru.edu.retro.dbservice.repositories.VoteRepository;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class DbEventService {
    private final BoardRepository boardRepository;
    private final ComponentRepository componentRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    @KafkaListener(topics = "db-event", groupId = "db-event-group")
    public void listen(KafkaEvent<?> event) {
        log.info("Received event: action={}, entity={}", event.action(), event.entity());
        try {
            if ("CREATE".equals(event.action())) {
                switch (event.entity()) {
                    case "Board":
                        boardCreate((BoardDtoKafka) event.payload());
                        break;
                    case "Component":
                        componentCreate((ComponentDtoKafka) event.payload());
                        break;
                    case "Vote":
                        voteCreate((VoteDtoKafka) event.payload());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown entity: " + event.entity() + " for " + event);
                }
            } else if ("UPDATE".equals(event.action())) {
                switch (event.entity()) {
                    case "Board":
                        boardUpdate((BoardDtoKafka) event.payload());
                        break;
                    case "Component":
                        componentUpdate((ComponentDtoKafka) event.payload());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown entity: " + event.entity() + " for " + event);
                }
            } else if ("DELETE".equals(event.action())) {
                switch (event.entity()) {
                    case "Board":
                        boardDelete((BoardDtoKafka) event.payload());
                        break;
                    case "Component":
                        componentDelete((ComponentDtoKafka) event.payload());
                        break;
                    case "Vote":
                        voteDelete((VoteDtoKafka) event.payload());
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

    private void boardUpdate(BoardDtoKafka boardDto) {
        try {
            var board = boardRepository.findById(boardDto.id()).orElseThrow(() -> new EntityNotFoundException("Board not found"));
            board.setTitle(boardDto.title());
            board.setIsProgress(boardDto.isProgress());
            board.setEndedAt(boardDto.endedAt());
            board.setInviteEditToken(boardDto.inviteEditToken());
            board.setEditors(new HashSet<>(userRepository.findAllById(boardDto.editorsId())));
            boardRepository.save(board);

            log.info("Board update: {}", boardDto.id());
        } catch (Exception e) {
            log.error("Failed to update Board: {}", boardDto.id(), e);
        }
    }

    private void componentUpdate(ComponentDtoKafka componentDto) {
        try {
            var component = componentRepository.findById(componentDto.id()).orElseThrow(() -> new EntityNotFoundException("Component not found"));
            component.setTitle(componentDto.title());
            component.setDescription(componentDto.description());
            component.setX(componentDto.x());
            component.setY(componentDto.y());
            component.setIsAnonymousAuthor(componentDto.isAnonymousAuthor());
            component.setIsAnonymousVotes(componentDto.isAnonymousVotes());
            componentRepository.save(component);

            log.info("Component update: {}", componentDto.id());
        } catch (Exception e) {
            log.error("Failed to update Component: {}", componentDto.id(), e);
        }
    }

    private void boardCreate(BoardDtoKafka boardDto) {
        Board board = new Board();
        try {
            board.setId(boardDto.id());
            board.setTitle(boardDto.title());
            board.setIsProgress(boardDto.isProgress());
            board.setCreatedAt(boardDto.createdAt());
            board.setEndedAt(boardDto.endedAt());
            board.setInviteEditToken(boardDto.inviteEditToken());
            userRepository.findById(boardDto.userId()).ifPresent(board::setAuthor);
            boardRepository.save(board);
            log.info("Board saved or updated: {}", board.getId());
        } catch (Exception e) {
            log.error("Failed to save or update Board: {}", board.getId(), e);
        }
    }

    private void boardDelete(BoardDtoKafka boardDto) {
        try {
            boardRepository.deleteById(boardDto.id());
            log.info("Board deleted: {}", boardDto.id());
        } catch (Exception e) {
            log.error("Failed to delete Board: {}", boardDto.id(), e);
        }
    }

    private void componentCreate(ComponentDtoKafka componentDto) {
        Component component = new Component();
        try {
            component.setId(componentDto.id());
            component.setTitle(componentDto.title());
            component.setDescription(componentDto.description());
            component.setType(componentDto.type());
            component.setX(componentDto.x());
            component.setY(componentDto.y());
            component.setIsAnonymousVotes(componentDto.isAnonymousVotes());
            component.setIsAnonymousAuthor(componentDto.isAnonymousAuthor());
            boardRepository.findById(componentDto.boardId()).ifPresent(component::setBoard);
            userRepository.findById(componentDto.authorId()).ifPresent(component::setAuthor);
            componentRepository.save(component);
            log.info("Component saved or updated: {}", component.getId());
        } catch (Exception e) {
            log.error("Failed to save or update Component: {}", component.getId(), e);
        }
    }

    private void componentDelete(ComponentDtoKafka componentDto) {
        try {
            componentRepository.deleteById(componentDto.id());
            log.info("Component deleted: {}", componentDto.id());
        } catch (Exception e) {
            log.error("Failed to delete Component: {}", componentDto.id(), e);
        }
    }

    private void voteCreate(VoteDtoKafka voteDto) {
        Vote vote = new Vote();
        try {
            userRepository.findById(voteDto.userId()).ifPresent(vote::setUser);
            componentRepository.findById(voteDto.componentId()).ifPresent(vote::setComponent);
            voteRepository.save(vote);
            log.info("Vote saved or updated: {}", vote.getId());
        } catch (Exception e) {
            log.error("Failed to save or update Vote: {}", vote.getId(), e);
        }
    }

    private void voteDelete(VoteDtoKafka voteDto) {
        try {
            voteRepository.deleteByUserIdAndComponentId(voteDto.userId(), voteDto.componentId());
            log.info("Vote deleted: userID={} componentId={}", voteDto.userId(), voteDto.componentId());
        } catch (Exception e) {
            log.error("Failed to delete Vote: userID={} componentId={}", voteDto.userId(), voteDto.componentId(), e);
        }
    }
}

