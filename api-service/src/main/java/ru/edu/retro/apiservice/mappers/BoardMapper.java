package ru.edu.retro.apiservice.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.edu.retro.apiservice.models.db.Board;
import ru.edu.retro.apiservice.models.db.User;
import ru.edu.retro.apiservice.models.dto.kafka.BoardDtoKafka;
import ru.edu.retro.apiservice.models.dto.requests.BoardRequest;
import ru.edu.retro.apiservice.models.dto.responses.BoardResponse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface BoardMapper {
    Board toBoard(BoardRequest boardRequest);
    BoardResponse toBoardResponse(Board board);

    @Mapping(source = "author.id", target = "userId")
    @Mapping(source = "editors", target = "editorsId")
    BoardDtoKafka toBoardDtoKafka(Board board);

    default List<Long> mapEditors(Set<User> editors) {
        return editors == null
                ? null
                : editors.stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }
}
