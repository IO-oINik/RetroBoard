package ru.edu.retro.apiservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.edu.retro.apiservice.exceptions.AuthException;
import ru.edu.retro.apiservice.exceptions.BadRequestException;
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
import ru.edu.retro.apiservice.models.dto.responses.UserResponse;
import ru.edu.retro.apiservice.repositories.BoardRepositoryReadOnly;
import ru.edu.retro.apiservice.repositories.SVGTemplateRepositoryReadOnly;
import ru.edu.retro.apiservice.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepositoryReadOnly boardRepository;
    private final SVGTemplateRepositoryReadOnly svgTemplateRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ComponentMapper componentMapper;
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, KafkaEvent<?>> kafkaTemplate;

    public BoardResponse getBoardById(UUID id) {
        return boardMapper
                .toBoardResponse(boardRepository
                        .findById(id).
                        orElseThrow(() -> new EntityNotFoundException("Board not found with the ID: " + id)));
    }

    public List<BoardResponse> getMyBoards() {
        var user = userService.findMe();
        var boards = boardRepository.findBoardsByAuthorId(user.id());
        return boards.stream().map(boardMapper::toBoardResponse).toList();
    }

    public BoardInviteToken getInviteTokenById(UUID id) {
        var board = boardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Board not found with the ID: " + id));
        return new BoardInviteToken(board.getInviteEditToken());
    }

    public List<UserResponse> getEditorsByBoardId(UUID id) {
        var board = boardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Board not found with the ID: " + id));
        return board.getEditors().stream().map(userMapper::toUserResponse).toList();
    }

    public BoardResponse createBoard(BoardRequest boardRequest) {
        var board = boardMapper.toBoard(boardRequest);
        board.setId(UUID.randomUUID());
        board.setAuthor(findMe());
        board.setIsProgress(true);
        board.setInviteEditToken(UUID.randomUUID());
        board.setCreatedAt(LocalDateTime.now());

        kafkaTemplate.send("db-event", new KafkaEvent<Board>("Board", "CREATE", board));
        return boardMapper.toBoardResponse(board);
    }

    public void deleteBoardById(UUID id) {
        var user = findMe();

        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            return;
        }
        var board = optionalBoard.get();

        if(!user.getId().equals(board.getAuthor().getId())) {
            throw new ForbiddenException("User is forbidden from deleting the board with ID: " + board.getId());
        }

        kafkaTemplate.send("db-event", new KafkaEvent<>("Board", "DELETE", board));
    }

    public void addEditor(UUID id, BoardInviteToken token) {
        var user = findMe();

        var board = boardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Board not found with ID: " + id));
        if (!board.getInviteEditToken().equals(token.token())) {
            throw new InvalidInviteTokenException("Invalid invite token");
        }

        if (!board.getAuthor().equals(user) && !board.getEditors().contains(user)) {
            board.getEditors().add(user);
            kafkaTemplate.send("db-event", new KafkaEvent<>("Board", "UPDATE", board));
        }
    }

    public BoardInviteToken generateNewInviteToken(UUID id) {
        var user = findMe();

        var board = boardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Board not found with ID: " + id));
        if (!board.getAuthor().equals(user)) {
            throw new ForbiddenException("User is forbidden to generate new invite token");
        }
        var inviteToken = new BoardInviteToken(UUID.randomUUID());
        board.setInviteEditToken(inviteToken.token());
        kafkaTemplate.send("db-event", new KafkaEvent<>("Board", "UPDATE", board));
        return inviteToken;
    }

    public void deleteEditor(UUID boardId, Long editorId) {
        var user = findMe();

        var board = boardRepository.findById(boardId).orElseThrow(() -> new EntityNotFoundException("Board not found with ID: " + boardId));
        if (!board.getAuthor().equals(user)) {
            throw new ForbiddenException("User is forbidden to delete editor");
        }

        if (board.getEditors().contains(user)) {
            board.getEditors().remove(user);
            kafkaTemplate.send("db-event", new KafkaEvent<>("Board", "UPDATE", board));
        }
    }

    public ComponentResponse createComponent(UUID boardId, ComponentRequest componentRequest) {
        var user = findMe();

        var board = boardRepository.findById(boardId).orElseThrow(() -> new EntityNotFoundException("Board not found with ID: " + boardId));
        if (!board.getEditors().contains(user) && !board.getAuthor().equals(user)) {
            throw new ForbiddenException("User is forbidden to create component");
        }

        var svgTemplate = svgTemplateRepository.findById(componentRequest.sourceId()).orElseThrow(() -> new BadRequestException("Source not found with ID: " + componentRequest.sourceId()));
        var component = componentMapper.toComponent(componentRequest);
        component.setId(UUID.randomUUID());
        component.setAuthor(user);
        component.setBoard(board);
        component.setSource(svgTemplate);

        kafkaTemplate.send("db-event", new KafkaEvent<>("Component", "CREATE", component));
        return componentMapper.toComponentResponse(component, userMapper);
    }

    private User findMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login;
        if (authentication != null) {
            login = (String) authentication.getPrincipal();
        } else {
            throw new AuthException("Unauthorized");
        }
        return userRepository.findByLogin(login).orElseThrow(() -> new EntityNotFoundException("User not found with the login: " + login));
    }
}
