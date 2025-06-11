package ru.edu.retro.apiservice.models;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.edu.retro.apiservice.models.dto.responses.SseEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class BoardEventsUsers {
    private final List<SseEmitter> emitters;
    @Getter
    private volatile LocalDateTime lastUpdate;

    public BoardEventsUsers() {
        emitters = new CopyOnWriteArrayList<>();
        lastUpdate = LocalDateTime.now();
    }

    private void updateLastActive() {
        lastUpdate = LocalDateTime.now();
    }

    public void sendAll(SseEvent<?> data) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(data, MediaType.APPLICATION_JSON);
            } catch (IOException e) {
                emitter.completeWithError(new RuntimeException("Error sending message"));
                emitters.remove(emitter);
                log.error("Error sending message", e);
            }
        });
        updateLastActive();
    }

    public void addEmitter(SseEmitter emitter) {
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitters.add(emitter);
        updateLastActive();
    }

    public void completeEmitters() {
        emitters.forEach(SseEmitter::complete);
        emitters.clear();
    }

}
