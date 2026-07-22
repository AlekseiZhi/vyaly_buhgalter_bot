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
import java.util.List;

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

        List<Participation> manualParticipations =
                findUnlinkedManualParticipations(session, telegramUserId, username);
        var activeParticipation = participationRepository
                .findByGameSessionIdAndTelegramUserIdAndLeftAtIsNull(session.getId(), telegramUserId);

        if (activeParticipation.isPresent()) {
            if (!manualParticipations.isEmpty()) {
                linkToTelegramAccount(manualParticipations, telegramUserId, username);
                return activeParticipation.get();
            }
            throw new AlreadyJoinedException(telegramUserId);
        }

        linkToTelegramAccount(manualParticipations, telegramUserId, username);

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

    @Override
    public Participation addManualPlayer(Long chatId, String username, Duration duration) {
        GameSession session = findActiveSession(chatId);
        Long telegramUserId = participationRepository
                .findAllByGameSessionIdAndUsernameIgnoreCase(session.getId(), username)
                .stream()
                .map(Participation::getTelegramUserId)
                .filter(id -> id > 0)
                .findFirst()
                .orElseGet(() -> Participation.syntheticId(username));

        Participation p = Participation.manual(
                session, telegramUserId, username, duration, Instant.now(clock));
        log.info("Manual player '{}' added to session {} with duration {}min", username, session.getId(), duration.toMinutes());
        return participationRepository.save(p);
    }

    private List<Participation> findUnlinkedManualParticipations(
            GameSession session,
            Long telegramUserId,
            String username) {
        return participationRepository
                .findAllByGameSessionIdAndUsernameIgnoreCase(session.getId(), username)
                .stream()
                .filter(Participation::isManual)
                .filter(p -> !telegramUserId.equals(p.getTelegramUserId()))
                .toList();
    }

    private void linkToTelegramAccount(
            List<Participation> participations,
            Long telegramUserId,
            String username) {
        participations.forEach(p -> p.linkToTelegramAccount(telegramUserId, username));
        if (!participations.isEmpty()) {
            log.info("Linked {} manual participation(s) for {} to Telegram user {}",
                    participations.size(), username, telegramUserId);
        }
    }

    private GameSession findActiveSession(Long chatId) {
        return gameSessionRepository
                .findByChatIdAndStatus(chatId, GameSessionStatus.ACTIVE)
                .orElseThrow(() -> new NoActiveGameException(chatId));
    }
}
