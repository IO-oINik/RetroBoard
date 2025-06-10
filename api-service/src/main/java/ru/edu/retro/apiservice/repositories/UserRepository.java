package ru.edu.retro.apiservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.retro.apiservice.models.db.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    Boolean existsByLogin(String login);
    Boolean existsByNickname(String nickname);
}
