package ru.vyaly_buhgalter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vyaly_buhgalter.domain.GameSession;
import ru.vyaly_buhgalter.domain.GameSessionStatus;
import ru.vyaly_buhgalter.domain.Participation;
import ru.vyaly_buhgalter.exception.AlreadyJoinedException;
import ru.vyaly_buhgalter.exception.NoActiveGameException;
import ru.vyaly_buhgalter.exception.NotInGameException;
import ru.vyaly_buhgalter.repository.GameSessionRepository;
import ru.vyaly_buhgalter.repository.ParticipationRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@Transactional
public class ParticipationServiceImpl implements ParticipationService {

    private final GameSessionRepository gameSessionRepository;
    private final ParticipationRepository participationRepository;
    private final Clock clock;

    public ParticipationServiceImpl(
            GameSessionRepository gameSessionRepository,
            ParticipationRepository participationRepository,
            Clock clock) {
        this.gameSessionRepository = gameSessionRepository;
        this.participationRepository = participationRepository;
        this.clock = clock;
    }

    @Override
    public Participation joinGame(Long chatId, Long telegramUserId, String username) {
        GameSession session = findActiveSession(chatId);

        participationRepository
                .findByGameSessionIdAndTelegramUserIdAndLeftAtIsNull(session.getId(), telegramUserId)
                .ifPresent(p -> { throw new AlreadyJoinedException(telegramUserId); });

        Participation participation = Participation.join(session, telegramUserId, username, Instant.now(clock));
        log.info("User {} ({}) joined session {}", username, telegramUserId, session.getId());
        return participationRepository.save(participation);
    }

    @Override
    public Duration leaveGame(Long chatId, Long telegramUserId) {
        GameSession session = findActiveSession(chatId);

        Participation participation = participationRepository
                .findByGameSessionIdAndTelegramUserIdAndLeftAtIsNull(session.getId(), telegramUserId)
                .orElseThrow(() -> new NotInGameException(telegramUserId));

        Duration duration = participation.leave(Instant.now(clock));
        log.info("User {} left session {}, duration={}m", telegramUserId, session.getId(), duration.toMinutes());
        return duration;
    }

    private GameSession findActiveSession(Long chatId) {
        return gameSessionRepository
                .findByChatIdAndStatus(chatId, GameSessionStatus.ACTIVE)
                .orElseThrow(() -> new NoActiveGameException(chatId));
    }
}
