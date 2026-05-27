package ru.vyaly_buhgalter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vyaly_buhgalter.domain.GameSession;
import ru.vyaly_buhgalter.domain.GameSessionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {

    Optional<GameSession> findByChatIdAndStatus(Long chatId, GameSessionStatus status);

    boolean existsByChatIdAndStatus(Long chatId, GameSessionStatus status);

    List<GameSession> findByChatIdAndStatusOrderByStartedAtDesc(Long chatId, GameSessionStatus status);
}
