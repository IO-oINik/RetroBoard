package ru.edu.retro.dbservice.configs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.hibernate.type.SerializationException;
import ru.edu.retro.dbservice.models.db.Board;
import ru.edu.retro.dbservice.models.db.Component;
import ru.edu.retro.dbservice.models.db.Vote;
import ru.edu.retro.dbservice.models.dto.KafkaEvent;
import ru.edu.retro.dbservice.models.dto.kafka.BoardDtoKafka;
import ru.edu.retro.dbservice.models.dto.kafka.ComponentDtoKafka;
import ru.edu.retro.dbservice.models.dto.kafka.VoteDtoKafka;

import java.util.Map;

@Slf4j
public class KafkaEventDeserializer implements Deserializer<KafkaEvent<?>> {
    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, Class<?>> typeMap = Map.of(
            "Component", ComponentDtoKafka.class,
            "Board", BoardDtoKafka.class,
            "Vote", VoteDtoKafka.class
    );

    @Override
    public KafkaEvent<?> deserialize(String topic, @Nullable byte[] data) {
        log.debug("Start deserialization of data for topic {}", topic);
        try {
            JsonNode jsonNode = mapper.readTree(data);
            mapper.registerModule(new JavaTimeModule());

            String entity = jsonNode.get("entity").asText();
            log.debug("Detected entity type: {}", entity);

            Class<?> payloadClass = typeMap.get(entity);
            if (payloadClass == null) {
                log.error("Unknown entity type: {}", entity);
                throw new IllegalArgumentException("Unknown entity type: " + entity);
            }

            JsonNode payloadNode = jsonNode.get("payload");
            Object payload = mapper.treeToValue(payloadNode, payloadClass);

            KafkaEvent<?> event = new KafkaEvent<>(
                    entity,
                    jsonNode.get("action").asText(),
                    payload
            );

            log.debug("Successfully deserialized event: {}", event);
            return event;

        } catch (Exception e) {
            log.error("Failed to deserialize data for topic {}", topic, e);
            throw new SerializationException("Failed to deserialize data", e);
        }
    }
}
