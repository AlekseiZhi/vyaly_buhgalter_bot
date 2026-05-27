package ru.vyaly_buhgalter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vyaly_buhgalter.domain.GameSession;
import ru.vyaly_buhgalter.domain.GameSessionStatus;
import ru.vyaly_buhgalter.exception.GameAlreadyActiveException;
import ru.vyaly_buhgalter.repository.GameSessionRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class GameSessionServiceImpl implements GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final Clock clock;

    public GameSessionServiceImpl(GameSessionRepository gameSessionRepository, Clock clock) {
        this.gameSessionRepository = gameSessionRepository;
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
    public Optional<GameSession> finishGame(Long chatId) {
        return gameSessionRepository.findByChatIdAndStatus(chatId, GameSessionStatus.ACTIVE)
                .map(session -> {
                    session.finish(Instant.now(clock));
                    log.info("Game session finished: id={}, chatId={}", session.getId(), chatId);
                    return session;
                });
    }
}
