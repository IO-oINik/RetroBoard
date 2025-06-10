package ru.edu.retro.apiservice.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.edu.retro.apiservice.models.db.User;
import ru.edu.retro.apiservice.models.dto.requests.RegisterRequest;
import ru.edu.retro.apiservice.models.dto.responses.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
    User toUser(UserResponse userResponse);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true)
    User toUser(RegisterRequest registerRequest);
}
