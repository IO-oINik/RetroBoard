package ru.edu.retro.apiservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.edu.retro.apiservice.models.dto.requests.BoardRequest;
import ru.edu.retro.apiservice.models.dto.requests.ComponentRequest;
import ru.edu.retro.apiservice.models.dto.responses.BoardInviteToken;
import ru.edu.retro.apiservice.models.dto.responses.BoardResponse;
import ru.edu.retro.apiservice.models.dto.responses.ComponentResponse;
import ru.edu.retro.apiservice.models.dto.responses.UserResponse;
import ru.edu.retro.apiservice.services.BoardService;
import ru.edu.retro.apiservice.services.ComponentService;
import ru.edu.retro.apiservice.services.SseEmitterService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
@Tag(name = "Board")
public class BoardController {
    private final BoardService boardService;
    private final ComponentService componentService;
    private final SseEmitterService sseEmitterService;

    @Operation(summary = "Подписаться на обновления доски")
    @GetMapping("/{id}/events")
    public SseEmitter createEmitter(@PathVariable UUID id) {
        return sseEmitterService.addEmitter(id);
    }

    @Operation(summary = "Создать доску")
    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@Valid @RequestBody BoardRequest boardRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(boardRequest));
    }

    @Operation(summary = "Получить доску по ID")
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> getById(@PathVariable(name = "id") UUID id) {
        return ResponseEntity.ok(boardService.getBoardById(id));
    }

    @Operation(summary = "Удалить доску по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable(name = "id") UUID id) {
        boardService.deleteBoardById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить все мои доски")
    @GetMapping("/me")
    public ResponseEntity<List<BoardResponse>> getMyBoards() {
        return ResponseEntity.ok(boardService.getMyBoards());
    }

    @Operation(summary = "Получить токен для редактирования доски")
    @GetMapping("/{id}/invite")
    public ResponseEntity<BoardInviteToken> getTokenInvite(@PathVariable(name = "id") UUID id) {
        return ResponseEntity.ok(boardService.getInviteTokenById(id));
    }

    @Operation(summary = "Получить доступ к редактированию доски по инвайт токену")
    @PostMapping("/{id}/invite")
    public ResponseEntity<Void> addEditor(@PathVariable(name = "id") UUID id, @Valid @RequestBody BoardInviteToken request) {
        boardService.addEditor(id, request);
        return ResponseEntity.status(200).build();
    }

    @Operation(summary = "Сгенерировать новый инвайт токен (предыдущий не работает)")
    @GetMapping("/{id}/invite/generate-token")
    public ResponseEntity<BoardInviteToken> generateToken(@PathVariable(name = "id") UUID id) {
        return ResponseEntity.status(200).body(boardService.generateNewInviteToken(id));
    }

    @Operation(summary = "Список редакторов")
    @GetMapping("/{id}/editors")
    public ResponseEntity<List<UserResponse>> getEditors(@PathVariable(name = "id") UUID id) {
        return ResponseEntity.ok(boardService.getEditorsByBoardId(id));
    }

    @Operation(summary = "Удалить редактора по его ID")
    @DeleteMapping("/{boardId}/editors/{editorId}")
    public ResponseEntity<Void> deleteEditor(@PathVariable(name = "boardId") UUID boardId, @PathVariable(name = "editorId") Long id) {
        boardService.deleteEditor(boardId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Создать новый компонент на доске")
    @PostMapping("/{boardId}/components")
    public ResponseEntity<ComponentResponse> createComponent(@PathVariable(name = "boardId") UUID boardId, @Valid @RequestBody ComponentRequest componentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createComponent(boardId, componentRequest));
    }

    @Operation(summary = "Получить все компоненты доски")
    @GetMapping("/{boardId}/components")
    public ResponseEntity<List<ComponentResponse>> getComponents(@PathVariable(name = "boardId") UUID boardId) {
        return ResponseEntity.ok(componentService.getComponentsByBoardId(boardId));
    }
}
