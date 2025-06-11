package ru.edu.retro.apiservice.models.dto.responses;

import lombok.Data;
import ru.edu.retro.apiservice.models.db.ComponentType;

import java.util.List;
import java.util.UUID;

@Data
public class ComponentResponse {
    private UUID id;
    private String title;
    private String description;
    private SVGTemplateResponse source;
    private UserResponse author;
    private ComponentType type;
    private Float x;
    private Float y;
    private Boolean isAnonymousAuthor;
    private Boolean isAnonymousVotes;
    private Integer countVotes;
    private List<UserResponse> votes;
}
