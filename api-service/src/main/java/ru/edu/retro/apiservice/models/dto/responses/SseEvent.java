package ru.edu.retro.apiservice.models.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SseEvent<T> {
    private String entity;
    private String action;
    private T data;
}
