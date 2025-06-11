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
import ru.edu.retro.apiservice.mappers.ComponentMapper;
import ru.edu.retro.apiservice.mappers.UserMapper;
import ru.edu.retro.apiservice.models.db.User;
import ru.edu.retro.apiservice.models.db.Vote;
import ru.edu.retro.apiservice.models.dto.KafkaEvent;
import ru.edu.retro.apiservice.models.dto.requests.ComponentEditRequest;
import ru.edu.retro.apiservice.models.dto.responses.ComponentResponse;
import ru.edu.retro.apiservice.models.dto.responses.SseEvent;
import ru.edu.retro.apiservice.models.dto.responses.VoteResponse;
import ru.edu.retro.apiservice.repositories.BoardRepositoryReadOnly;
import ru.edu.retro.apiservice.repositories.ComponentRepositoryReadOnly;
import ru.edu.retro.apiservice.repositories.UserRepository;
import ru.edu.retro.apiservice.repositories.VoteRepositoryReadOnly;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentService {
    private final ComponentRepositoryReadOnly componentRepository;
    private final BoardRepositoryReadOnly boardRepository;
    private final VoteRepositoryReadOnly voteRepository;
    private final ComponentMapper componentMapper;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, KafkaEvent<?>> kafkaTemplate;
    private final UserMapper userMapper;
    private final SseEmitterService sseEmitterService;

    public List<ComponentResponse> getComponentsByBoardId(UUID id) {
        log.info("Fetching components for board ID: {}", id);
        var board = boardRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.warn("Board not found with ID: {}", id);
                    return new EntityNotFoundException("Board not found with ID: " + id);
                });
        var components = componentRepository.findComponentByBoardId(board.getId()).stream()
                .map(component -> componentMapper.toComponentResponse(component, userMapper))
                .toList();
        log.info("Found {} components for board ID: {}", components.size(), id);
        return components;
    }

    public ComponentResponse editById(UUID id, ComponentEditRequest componentEditRequest) {
        var user = findMe();
        log.info("User '{}' is editing component ID: {}", user.getLogin(), id);

        var component = componentRepository.findComponentById(id).orElseThrow(() -> {
            log.warn("Component not found with ID: {}", id);
            return new EntityNotFoundException("Component not found with ID: " + id);
        });

        if (!component.getBoard().getEditors().contains(user) && !component.getBoard().getAuthor().equals(user)) {
            log.warn("User '{}' forbidden to edit component ID: {}", user.getLogin(), id);
            throw new ForbiddenException("Component is not forbidden to edit");
        }

        component.setTitle(componentEditRequest.title());
        component.setDescription(componentEditRequest.description());
        component.setX(componentEditRequest.x());
        component.setY(componentEditRequest.y());

        kafkaTemplate.send("db-event", new KafkaEvent<>("Component", "EDIT", component));
        var componentResponse = componentMapper.toComponentResponse(component, userMapper);
        sseEmitterService.sendAll(component.getBoard().getId(), new SseEvent<>("Component", "EDIT", componentResponse));

        log.info("Component ID: {} edited successfully by user '{}'", id, user.getLogin());
        return componentResponse;
    }

    public void deleteById(UUID id) {
        var user = findMe();
        log.info("User '{}' is deleting component ID: {}", user.getLogin(), id);

        var optionalComponent = componentRepository.findComponentById(id);
        if (optionalComponent.isEmpty()) {
            log.warn("Component ID: {} not found, delete aborted", id);
            return;
        }
        var component = optionalComponent.get();

        if (!component.getBoard().getEditors().contains(user) && !component.getBoard().getAuthor().equals(user)) {
            log.warn("User '{}' forbidden to delete component ID: {}", user.getLogin(), id);
            throw new ForbiddenException("Component is not forbidden to delete");
        }

        kafkaTemplate.send("db-event", new KafkaEvent<>("Component", "DELETE", component));
        sseEmitterService.sendAll(component.getBoard().getId(),
                new SseEvent<>("Component",
                        "DELETE",
                        componentMapper.toComponentResponse(component, userMapper)));

        log.info("Component ID: {} deleted successfully by user '{}'", id, user.getLogin());
    }

    public void addVote(UUID idComponent) {
        var user = findMe();
        log.info("User '{}' adding vote to component ID: {}", user.getLogin(), idComponent);

        var component = componentRepository.findComponentById(idComponent).orElseThrow(() -> {
            log.warn("Component not found with ID: {}", idComponent);
            return new EntityNotFoundException("Component not found with ID: " + idComponent);
        });

        if (voteRepository.existsByComponentIdAndUserId(component.getId(), user.getId())) {
            log.info("User '{}' has already voted for component ID: {}, skipping", user.getLogin(), idComponent);
            return;
        }

        kafkaTemplate.send("db-event", new KafkaEvent<>("Vote", "CREATE", new Vote(user, component)));
        sseEmitterService.sendAll(component.getBoard().getId(), new SseEvent<>("Vote", "CREATE", new VoteResponse(component.getId())));

        log.info("Vote added by user '{}' to component ID: {}", user.getLogin(), idComponent);
    }

    public void removeVote(UUID idComponent) {
        var user = findMe();
        log.info("User '{}' removing vote from component ID: {}", user.getLogin(), idComponent);

        var optionalComponent = componentRepository.findComponentById(idComponent);
        if (optionalComponent.isEmpty()) {
            log.warn("Component ID: {} not found, remove vote aborted", idComponent);
            return;
        }

        var component = optionalComponent.get();
        var voteOptional = voteRepository.findByComponentIdAndUserId(component.getId(), user.getId());

        voteOptional.ifPresent(vote -> {
            kafkaTemplate.send("db-event", new KafkaEvent<>("Vote", "DELETE", vote));
            sseEmitterService.sendAll(component.getBoard().getId(), new SseEvent<>("Vote", "DELETE", new VoteResponse(component.getId())));
            log.info("Vote removed by user '{}' from component ID: {}", user.getLogin(), idComponent);
        });
    }

    private User findMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login;
        if (authentication != null) {
            login = (String) authentication.getPrincipal();
        } else {
            log.error("Unauthorized access attempt");
            throw new AuthException("Unauthorized");
        }
        return userRepository.findByLogin(login).orElseThrow(() -> {
            log.error("User not found with login: {}", login);
            return new EntityNotFoundException("User not found with the login: " + login);
        });
    }
}
