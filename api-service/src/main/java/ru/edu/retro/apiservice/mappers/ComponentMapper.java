package ru.edu.retro.apiservice.mappers;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.edu.retro.apiservice.models.db.Component;
import ru.edu.retro.apiservice.models.dto.kafka.ComponentDtoKafka;
import ru.edu.retro.apiservice.models.dto.requests.ComponentRequest;
import ru.edu.retro.apiservice.models.dto.responses.ComponentResponse;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ComponentMapper {
    Component toComponent(ComponentRequest componentRequest);

    @Mapping(target = "countVotes", expression = "java(component.getVotes() != null ? component.getVotes().size() : 0)")
    @Mapping(target = "votes", expression = "java(component.getIsAnonymousVotes() && component.getVotes() == null ? null : component.getVotes().stream().map(v -> userMapper.toUserResponse(v.getUser())).toList())")
    @Mapping(target = "author", expression = "java(component.getIsAnonymousAuthor() ? null : userMapper.toUserResponse(component.getAuthor()))")
    ComponentResponse toComponentResponse(Component component, @Context UserMapper userMapper);

    @Mapping(source = "board.id", target = "boardId")
    @Mapping(source = "author.id", target = "authorId")
    ComponentDtoKafka toComponentDtoKafka(Component component);
}