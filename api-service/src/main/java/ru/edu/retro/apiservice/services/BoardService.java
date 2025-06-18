package ru.edu.retro.apiservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.edu.retro.apiservice.exceptions.AuthException;
import ru.edu.retro.apiservice.exceptions.EntityNotFoundException;
import ru.edu.retro.apiservice.exceptions.ForbiddenException;
import ru.edu.retro.apiservice.exceptions.InvalidInviteTokenException;
import ru.edu.retro.apiservice.mappers.BoardMapper;
import ru.edu.retro.apiservice.mappers.ComponentMapper;
import ru.edu.retro.apiservice.mappers.UserMapper;
import ru.edu.retro.apiservice.models.db.Board;
import ru.edu.retro.apiservice.models.db.User;
import ru.edu.retro.apiservice.models.dto.KafkaEvent;
import ru.edu.retro.apiservice.models.dto.requests.BoardRequest;
import ru.edu.retro.apiservice.models.dto.requests.ComponentRequest;
import ru.edu.retro.apiservice.models.dto.responses.BoardInviteToken;
import ru.edu.retro.apiservice.models.dto.responses.BoardResponse;
import ru.edu.retro.apiservice.models.dto.responses.ComponentResponse;
import ru.edu.retro.apiservice.models.dto.responses.SseEvent;
import ru.edu.retro.apiservice.models.dto.responses.UserResponse;
import ru.edu.retro.apiservice.repositories.BoardRepositoryReadOnly;
import ru.edu.retro.apiservice.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepositoryReadOnly boardRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ComponentMapper componentMapper;
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, KafkaEvent<?>> kafkaTemplate;
    private final SseEmitterService sseEmitterService;

    public BoardResponse getBoardById(UUID id) {
        log.debug("Fetching board by ID: {}", id);
        return boardMapper.toBoardResponse(boardRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Board not found with ID: {}", id);
                    return new EntityNotFoundException("Board not found with the ID: " + id);
                }));
    }

    public List<BoardResponse> getMyBoards() {
        var user = findMe();
        log.debug("Fetching boards for user ID: {}", user.getId());
        var boards = boardRepository.findBoardsByAuthorId(user.getId());
        return boards.stream().map(boardMapper::toBoardResponse).toList();
    }

    public BoardInviteToken getInviteTokenById(UUID id) {
        var user = findMe();
        log.debug("Getting invite token for board ID: {}", id);
        var board = boardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Board not found with the ID: " + id));
        if (!board.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("User is forbidden to get invite token for this board");
        }
        return new BoardInviteToken(board.getInviteEditToken());
    }

    public List<UserResponse> getEditorsByBoardId(UUID id) {
        log.debug("Fetching editors for board ID: {}", id);
        var board = boardRepository.findById(id).orElseThrow(() -> {
            log.warn("Board not found with ID: {}", id);
            return new EntityNotFoundException("Board not found with the ID: " + id);
        });
        return board.getEditors().stream().map(userMapper::toUserResponse).toList();
    }

    public BoardResponse createBoard(BoardRequest boardRequest) {
        var board = boardMapper.toBoard(boardRequest);
        board.setId(UUID.randomUUID());
        board.setAuthor(findMe());
        board.setIsProgress(true);
        board.setInviteEditToken(UUID.randomUUID());
        board.setCreatedAt(LocalDateTime.now());

        log.info("Creating board with ID: {}", board.getId());
        kafkaTemplate.send("db-event", new KafkaEvent<>("Board", "CREATE", boardMapper.toBoardDtoKafka(board)));
        return boardMapper.toBoardResponse(board);
    }

    public void deleteBoardById(UUID id) {
        var user = findMe();
        log.debug("Attempting to delete board ID: {} by user ID: {}", id, user.getId());

        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            log.debug("Board not found with ID: {}, nothing to delete", id);
            return;
        }

        var board = optionalBoard.get();
        if (!user.getId().equals(board.getAuthor().getId())) {
            log.warn("User ID: {} is forbidden to delete board ID: {}", user.getId(), board.getId());
            throw new ForbiddenException("User is forbidden from deleting the board with ID: " + board.getId());
        }

        log.info("Deleting board ID: {}", id);
        kafkaTemplate.send("db-event", new KafkaEvent<>("Board", "DELETE", boardMapper.toBoardDtoKafka(board)));
    }

    public void addEditor(UUID id, BoardInviteToken token) {
        var user = findMe();
        log.debug("User ID: {} attempting to join board ID: {} with token", user.getId(), id);

        var board = boardRepository.findById(id).orElseThrow(() -> {
            log.warn("Board not found with ID: {}", id);
            return new EntityNotFoundException("Board not found with ID: " + id);
        });

        if (!board.getInviteEditToken().equals(token.token())) {
            log.warn("Invalid invite token used for board ID: {}", id);
            throw new InvalidInviteTokenException("Invalid invite token");
        }

        if (!board.getAuthor().equals(user) && !board.getEditors().contains(user)) {
            board.getEditors().add(user);
            log.info("User ID: {} added as editor to board ID: {}", user.getId(), id);
            kafkaTemplate.send("db-event", new KafkaEvent<>("Board", "UPDATE", boardMapper.toBoardDtoKafka(board)));
        } else {
            log.debug("User ID: {} is already an editor or the author of board ID: {}", user.getId(), id);
        }
    }

    public BoardInviteToken generateNewInviteToken(UUID id) {
        var user = findMe();
        log.debug("User ID: {} attempting to generate new token for board ID: {}", user.getId(), id);

        var board = boardRepository.findById(id).orElseThrow(() -> {
            log.warn("Board not found with ID: {}", id);
            return new EntityNotFoundException("Board not found with ID: " + id);
        });

        if (!board.getAuthor().equals(user)) {
            log.warn("User ID: {} is forbidden to generate token for board ID: {}", user.getId(), id);
            throw new ForbiddenException("User is forbidden to generate new invite token");
        }

        var inviteToken = new BoardInviteToken(UUID.randomUUID());
        board.setInviteEditToken(inviteToken.token());
        log.info("Generated new invite token for board ID: {}", id);
        kafkaTemplate.send("db-event", new KafkaEvent<>("Board", "UPDATE", boardMapper.toBoardDtoKafka(board)));
        return inviteToken;
    }

    public void deleteEditor(UUID boardId, Long editorId) {
        var user = findMe();
        log.debug("User ID: {} attempting to delete editor ID: {} from board ID: {}", user.getId(), editorId, boardId);

        var board = boardRepository.findById(boardId).orElseThrow(() -> {
            log.warn("Board not found with ID: {}", boardId);
            return new EntityNotFoundException("Board not found with ID: " + boardId);
        });

        if (!board.getAuthor().equals(user)) {
            log.warn("User ID: {} is forbidden to delete editors from board ID: {}", user.getId(), boardId);
            throw new ForbiddenException("User is forbidden to delete editor");
        }

        board.getEditors().removeIf(e -> e.getId().equals(editorId));
        log.info("Editor ID: {} removed from board ID: {}", editorId, boardId);
        kafkaTemplate.send("db-event", new KafkaEvent<>("Board", "UPDATE", boardMapper.toBoardDtoKafka(board)));
    }

    public ComponentResponse createComponent(UUID boardId, ComponentRequest componentRequest) {
        var user = findMe();
        log.debug("User ID: {} creating component on board ID: {}", user.getId(), boardId);

        var board = boardRepository.findById(boardId).orElseThrow(() -> {
            log.warn("Board not found with ID: {}", boardId);
            return new EntityNotFoundException("Board not found with ID: " + boardId);
        });

        if (!board.getEditors().contains(user) && !board.getAuthor().equals(user)) {
            log.warn("User ID: {} is forbidden to create component on board ID: {}", user.getId(), boardId);
            throw new ForbiddenException("User is forbidden to create component");
        }

        var component = componentMapper.toComponent(componentRequest);
        component.setId(UUID.randomUUID());
        component.setAuthor(user);
        component.setBoard(board);
        component.setVotes(Collections.emptyList());

        kafkaTemplate.send("db-event", new KafkaEvent<>("Component", "CREATE", componentMapper.toComponentDtoKafka(component)));
        var componentResponse = componentMapper.toComponentResponse(component, userMapper);

        log.info("Component created with ID: {} on board ID: {}", component.getId(), boardId);
        sseEmitterService.sendAll(boardId, new SseEvent<>("Component", "CREATE", componentResponse));
        return componentResponse;
    }

    private User findMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login;
        if (authentication != null) {
            login = (String) authentication.getPrincipal();
        } else {
            log.warn("Unauthorized access attempt");
            throw new AuthException("Unauthorized");
        }

        return userRepository.findByLogin(login).orElseThrow(() -> {
            log.warn("User not found with login: {}", login);
            return new EntityNotFoundException("User not found with the login: " + login);
        });
    }
}
