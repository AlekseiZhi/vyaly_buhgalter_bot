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

    Optional<Participation> findByGameSessionIdAndTelegramUserIdAndLeftAtIsNull(UUID gameSessionId, Long telegramUserId);

    int countByGameSessionId(UUID gameSessionId);

    @Query("SELECT p FROM Participation p WHERE p.gameSession.chatId = :chatId AND p.gameSession.status = :status")
    List<Participation> findAllByGameSessionChatIdAndStatus(@Param("chatId") Long chatId, @Param("status") GameSessionStatus status);
}
