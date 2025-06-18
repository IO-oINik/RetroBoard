package ru.edu.retro.dbservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.retro.dbservice.models.db.Component;

import java.util.UUID;

@Repository
public interface ComponentRepository extends JpaRepository<Component, UUID> {
}
