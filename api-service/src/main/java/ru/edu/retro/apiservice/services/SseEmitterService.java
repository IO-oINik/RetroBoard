package ru.edu.retro.apiservice.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.edu.retro.apiservice.models.BoardEventsUsers;
import ru.edu.retro.apiservice.models.dto.responses.SseEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {
    @Value("${sse.timeout}")
    private Long SSE_EMITTER_TIMEOUT;

    private final Map<UUID, BoardEventsUsers> boardEventsUsersMap;

    public SseEmitterService() {
        boardEventsUsersMap = new ConcurrentHashMap<>();
    }

    public SseEmitter addEmitter(UUID boardId) {
        log.info("Adding SSE emitter for boardId: {}", boardId);
        var sseEmitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
        BoardEventsUsers boardEventsUsers = boardEventsUsersMap.get(boardId);
        if (boardEventsUsers == null) {
            boardEventsUsers = new BoardEventsUsers();
            boardEventsUsers.addEmitter(sseEmitter);
            boardEventsUsersMap.put(boardId, boardEventsUsers);
            log.info("Created new BoardEventsUsers and added emitter for boardId: {}", boardId);
        } else {
            boardEventsUsers.addEmitter(sseEmitter);
            log.info("Added emitter to existing BoardEventsUsers for boardId: {}", boardId);
        }
        return sseEmitter;
    }

    public void removeEmitters(UUID boardId) {
        log.info("Removing SSE emitters for boardId: {}", boardId);
        BoardEventsUsers boardEventsUsers = boardEventsUsersMap.get(boardId);
        if (boardEventsUsers != null) {
            boardEventsUsers.completeEmitters();
            boardEventsUsersMap.remove(boardId);
            log.info("Removed and completed emitters for boardId: {}", boardId);
        } else {
            log.warn("No emitters found to remove for boardId: {}", boardId);
        }
    }

    public void sendAll(UUID boardId, SseEvent<?> data) {
        log.info("Sending SSE event to all emitters for boardId: {}", boardId);
        BoardEventsUsers boardEventsUsers = boardEventsUsersMap.get(boardId);
        if (boardEventsUsers != null) {
            boardEventsUsers.sendAll(data);
            log.info("Event sent to all emitters for boardId: {}", boardId);
        } else {
            log.warn("No emitters to send event for boardId: {}", boardId);
        }
    }

    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 минут
    private void removeOldEmitters() {
        log.info("Scheduled cleanup of old SSE emitters started");
        for (Map.Entry<UUID, BoardEventsUsers> entry : boardEventsUsersMap.entrySet()) {
            if (entry.getValue().getLastUpdate()
                    .isBefore(LocalDateTime.now().minusNanos(SSE_EMITTER_TIMEOUT * 1_000_000))) {
                log.info("Removing old emitters for boardId: {}", entry.getKey());
                removeEmitters(entry.getKey());
            }
        }
        log.info("Scheduled cleanup of old SSE emitters finished");
    }
}