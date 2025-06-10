package ru.edu.retro.apiservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.edu.retro.apiservice.models.dto.KafkaEvent;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, KafkaEvent<?>> kafkaTemplate(ProducerFactory<String, KafkaEvent<?>> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
