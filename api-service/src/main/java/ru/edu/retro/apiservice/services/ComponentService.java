package ru.edu.retro.apiservice.services;

import lombok.RequiredArgsConstructor;
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
import ru.edu.retro.apiservice.repositories.BoardRepositoryReadOnly;
import ru.edu.retro.apiservice.repositories.ComponentRepositoryReadOnly;
import ru.edu.retro.apiservice.repositories.UserRepository;
import ru.edu.retro.apiservice.repositories.VoteRepositoryReadOnly;

import java.util.List;
import java.util.UUID;

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

    public List<ComponentResponse> getComponentsByBoardId(UUID id) {
        var board = boardRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Board not found with ID: " + id));
        return componentRepository
                .findComponentByBoardId(board.getId()).stream().map(component -> componentMapper.toComponentResponse(component, userMapper)).toList();
    }
    
    public ComponentResponse editById(UUID id, ComponentEditRequest componentEditRequest) {
        var user = findMe();

        var component = componentRepository.findComponentById(id).orElseThrow(() -> new EntityNotFoundException("Component not found with ID: " + id));
        if (!component.getBoard().getEditors().contains(user) && !component.getBoard().getAuthor().equals(user)) {
            throw new ForbiddenException("Component is not forbidden to edit");
        }

        component.setTitle(componentEditRequest.title());
        component.setDescription(componentEditRequest.description());
        component.setX(componentEditRequest.x());
        component.setY(componentEditRequest.y());

        kafkaTemplate.send("db-event", new KafkaEvent<>("Component", "EDIT", component));
        return componentMapper.toComponentResponse(component, userMapper);
    }

    public void deleteById(UUID id) {
        var user = findMe();

        var optionalComponent = componentRepository.findComponentById(id);
        if (optionalComponent.isEmpty()) {
            return;
        }
        var component = optionalComponent.get();
        if (!component.getBoard().getEditors().contains(user) && !component.getBoard().getAuthor().equals(user)) {
            throw new ForbiddenException("Component is not forbidden to delete");
        }

        kafkaTemplate.send("db-event", new KafkaEvent<>("Component", "DELETE", component));
    }

    public void addVote(UUID idComponent) {
        var user = findMe();

        var component = componentRepository.findComponentById(idComponent).orElseThrow(() -> new EntityNotFoundException("Component not found with ID: " + idComponent));
        if (voteRepository.existsByComponentIdAndUserId(component.getId(), user.getId())) {
            return;
        }

        kafkaTemplate.send("db-event", new KafkaEvent<>("Vote", "CREATE", new Vote(user, component)));
    }

    public void removeVote(UUID idComponent) {
        var user = findMe();

        var optionalComponent = componentRepository.findComponentById(idComponent);
        if (optionalComponent.isEmpty()) {
            return;
        }

        var component = optionalComponent.get();
        var voteOptional = voteRepository.findByComponentIdAndUserId(component.getId(), user.getId());

        voteOptional.ifPresent(vote -> kafkaTemplate.send("db-event", new KafkaEvent<>("Vote", "DELETE", vote)));
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
