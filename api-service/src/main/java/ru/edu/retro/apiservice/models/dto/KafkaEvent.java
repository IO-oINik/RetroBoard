package ru.edu.retro.apiservice.models.dto;

public record KafkaEvent<T>(
        String entity,
        String action,
        T payload
) {
}
