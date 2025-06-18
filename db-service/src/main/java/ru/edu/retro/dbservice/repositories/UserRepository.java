package ru.edu.retro.dbservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.retro.dbservice.models.db.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
