package ru.edu.retro.apiservice.mappers;

import org.mapstruct.Mapper;
import ru.edu.retro.apiservice.models.db.Board;
import ru.edu.retro.apiservice.models.dto.requests.BoardRequest;
import ru.edu.retro.apiservice.models.dto.responses.BoardResponse;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface BoardMapper {
    Board toBoard(BoardRequest boardRequest);
    BoardResponse toBoardResponse(Board board);
}
