package ru.edu.retro.dbservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class DbServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbServiceApplication.class, args);
    }

}
