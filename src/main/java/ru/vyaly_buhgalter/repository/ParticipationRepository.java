package ru.vyaly_buhgalter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vyaly_buhgalter.domain.GameSessionStatus;
import ru.vyaly_buhgalter.domain.Participation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    Optional<Participation> findByGameSessionIdAndTelegramUserId(UUID gameSessionId, Long telegramUserId);

    List<Participation> findAllByGameSessionId(UUID gameSessionId);

    List<Participation> findAllByGameSessionIdAndUsernameIgnoreCase(UUID gameSessionId, String username);

    boolean existsByGameSessionIdAndTelegramUserId(UUID gameSessionId, Long telegramUserId);

    Optional<Participation> findByGameSessionIdAndTelegramUserIdAndLeftAtIsNull(UUID gameSessionId, Long telegramUserId);

    @Query("SELECT COUNT(DISTINCT p.telegramUserId) FROM Participation p WHERE p.gameSession.id = :gameSessionId")
    int countDistinctPlayersByGameSessionId(@Param("gameSessionId") UUID gameSessionId);

    @Query("SELECT p FROM Participation p JOIN FETCH p.gameSession gs WHERE gs.chatId = :chatId AND gs.status = :status")
    List<Participation> findAllByGameSessionChatIdAndStatus(@Param("chatId") Long chatId, @Param("status") GameSessionStatus status);
}
