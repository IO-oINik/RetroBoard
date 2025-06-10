package ru.edu.retro.dbservice.models.dto;

public record KafkaEvent<T>(
        String entity,
        String action,
        T payload
) {
}
