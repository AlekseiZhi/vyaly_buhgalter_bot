package ru.vyaly_buhgalter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vyaly_buhgalter.domain.GameSession;
import ru.vyaly_buhgalter.domain.GameSessionStatus;
import ru.vyaly_buhgalter.domain.Participation;
import ru.vyaly_buhgalter.exception.GameAlreadyActiveException;
import ru.vyaly_buhgalter.exception.GameFinishForbiddenException;
import ru.vyaly_buhgalter.repository.GameSessionRepository;
import ru.vyaly_buhgalter.repository.ParticipationRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class GameSessionServiceImpl implements GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final ParticipationRepository participationRepository;
    private final Clock clock;

    public GameSessionServiceImpl(GameSessionRepository gameSessionRepository,
                                  ParticipationRepository participationRepository,
                                  Clock clock) {
        this.gameSessionRepository = gameSessionRepository;
        this.participationRepository = participationRepository;
        this.clock = clock;
    }

    @Override
    public GameSession startGame(Long chatId) {
        if (gameSessionRepository.existsByChatIdAndStatus(chatId, GameSessionStatus.ACTIVE)) {
            throw new GameAlreadyActiveException(chatId);
        }
        GameSession session = gameSessionRepository.save(GameSession.start(chatId, Instant.now(clock)));
        log.info("Game session started: id={}, chatId={}", session.getId(), chatId);
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameSession> getActiveGame(Long chatId) {
        return gameSessionRepository.findByChatIdAndStatus(chatId, GameSessionStatus.ACTIVE);
    }

    @Override
    public Optional<GameSession> finishGame(Long chatId, Long telegramUserId, String username) {
        return gameSessionRepository.findByChatIdAndStatus(chatId, GameSessionStatus.ACTIVE)
                .map(session -> {
                    verifyParticipantAndLinkManualEntries(session, telegramUserId, username);
                    session.finish(Instant.now(clock), telegramUserId, username);
                    log.info("Game session finished: id={}, chatId={}, byUser={}",
                            session.getId(), chatId, telegramUserId);
                    return session;
                });
    }

    private void verifyParticipantAndLinkManualEntries(
            GameSession session,
            Long telegramUserId,
            String username) {
        boolean knownById = participationRepository
                .existsByGameSessionIdAndTelegramUserId(session.getId(), telegramUserId);

        var matchingUsernameEntries = participationRepository
                .findAllByGameSessionIdAndUsernameIgnoreCase(session.getId(), username);

        if (!knownById && matchingUsernameEntries.isEmpty()) {
            throw new GameFinishForbiddenException(telegramUserId);
        }

        matchingUsernameEntries.stream()
                .filter(Participation::isManual)
                .filter(p -> !telegramUserId.equals(p.getTelegramUserId()))
                .forEach(p -> p.linkToTelegramAccount(telegramUserId, username));
    }
}
