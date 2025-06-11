package ru.edu.retro.apiservice.services;

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

@Service
public class SseEmitterService {
    @Value("${sse.timeout}")
    private Long SSE_EMITTER_TIMEOUT;
    private final Map<UUID, BoardEventsUsers> boardEventsUsersMap;

    public SseEmitterService() {
        boardEventsUsersMap = new ConcurrentHashMap<>();
    }

    public SseEmitter addEmitter(UUID boardId) {
        var sseEmitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
        BoardEventsUsers boardEventsUsers = boardEventsUsersMap.get(boardId);
        if (boardEventsUsers == null) {
            boardEventsUsers = new BoardEventsUsers();
            boardEventsUsers.addEmitter(sseEmitter);
            boardEventsUsersMap.put(boardId, boardEventsUsers);
        } else {
            boardEventsUsers.addEmitter(sseEmitter);
        }
        return sseEmitter;
    }

    public void removeEmitters(UUID boardId) {
        BoardEventsUsers boardEventsUsers = boardEventsUsersMap.get(boardId);
        if (boardEventsUsers != null) {
            boardEventsUsers.completeEmitters();
            boardEventsUsersMap.remove(boardId);
        }
    }

    public void sendAll(UUID boardId, SseEvent<?> data) {
        BoardEventsUsers boardEventsUsers = boardEventsUsersMap.get(boardId);
        if (boardEventsUsers != null) {
            boardEventsUsers.sendAll(data);
        }
    }

    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 минут
    private void removeOldEmitters() {
        for (Map.Entry<UUID, BoardEventsUsers> entry : boardEventsUsersMap.entrySet()) {
            if (entry.getValue().getLastUpdate().isBefore(LocalDateTime.now().minusNanos(SSE_EMITTER_TIMEOUT * 1_000_000))) {
                removeEmitters(entry.getKey());
            }
        }
    }

}
