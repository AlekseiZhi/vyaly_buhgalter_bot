package ru.vyaly_buhgalter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vyaly_buhgalter.domain.GameSession;
import ru.vyaly_buhgalter.domain.GameSessionStatus;
import ru.vyaly_buhgalter.dto.GameHistoryItem;
import ru.vyaly_buhgalter.repository.GameSessionRepository;
import ru.vyaly_buhgalter.repository.ParticipationRepository;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class GameHistoryServiceImpl implements GameHistoryService {

    private final GameSessionRepository gameSessionRepository;
    private final ParticipationRepository participationRepository;

    public GameHistoryServiceImpl(GameSessionRepository gameSessionRepository,
                                  ParticipationRepository participationRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.participationRepository = participationRepository;
    }

    @Override
    public List<GameHistoryItem> getLastGames(Long chatId, int limit) {
        return gameSessionRepository
                .findByChatIdAndStatusOrderByStartedAtDesc(chatId, GameSessionStatus.FINISHED)
                .stream()
                .limit(limit)
                .map(this::toHistoryItem)
                .toList();
    }

    private GameHistoryItem toHistoryItem(GameSession session) {
        int playersCount = participationRepository.countDistinctPlayersByGameSessionId(session.getId());
        Duration totalDuration = session.getEndedAt() != null
                ? Duration.between(session.getStartedAt(), session.getEndedAt())
                : Duration.ZERO;

        return new GameHistoryItem(
                session.getStartedAt(),
                session.getEndedAt(),
                session.getTotalCost(),
                totalDuration,
                playersCount,
                session.getFinishedByUsername());
    }
}
