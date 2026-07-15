package ru.vyaly_buhgalter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vyaly_buhgalter.domain.GameSession;
import ru.vyaly_buhgalter.domain.GameSessionStatus;
import ru.vyaly_buhgalter.domain.Participation;
import ru.vyaly_buhgalter.dto.GameStatus;
import ru.vyaly_buhgalter.dto.GameStatus.ParticipantStatus;
import ru.vyaly_buhgalter.repository.GameSessionRepository;
import ru.vyaly_buhgalter.repository.ParticipationRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GameStatusServiceImpl implements GameStatusService {

    private final GameSessionRepository gameSessionRepository;
    private final ParticipationRepository participationRepository;
    private final Clock clock;

    public GameStatusServiceImpl(GameSessionRepository gameSessionRepository,
                                 ParticipationRepository participationRepository,
                                 Clock clock) {
        this.gameSessionRepository = gameSessionRepository;
        this.participationRepository = participationRepository;
        this.clock = clock;
    }

    @Override
    public Optional<GameStatus> getActiveStatus(Long chatId) {
        return gameSessionRepository
                .findByChatIdAndStatus(chatId, GameSessionStatus.ACTIVE)
                .map(this::buildStatus);
    }

    private GameStatus buildStatus(GameSession session) {
        Instant now = Instant.now(clock);
        Duration gameDuration = Duration.between(session.getStartedAt(), now);

        List<Participation> participations =
                participationRepository.findAllByGameSessionId(session.getId());

        // Aggregate multiple join/leave cycles per user into one status entry.
        Map<Long, List<Participation>> byUser = participations.stream()
                .collect(Collectors.groupingBy(Participation::getTelegramUserId));

        List<ParticipantStatus> statuses = byUser.values().stream()
                .map(parts -> toParticipantStatus(parts, now))
                .sorted(Comparator
                        .comparing(ParticipantStatus::active).reversed()
                        .thenComparing(ParticipantStatus::totalTime).reversed())
                .toList();

        return new GameStatus(session.getStartedAt(), gameDuration, statuses);
    }

    private ParticipantStatus toParticipantStatus(List<Participation> parts, Instant now) {
        String username = parts.getLast().getUsername();
        Duration total = parts.stream()
                .map(p -> p.computeDuration(now))
                .reduce(Duration.ZERO, Duration::plus);
        boolean active = parts.stream().anyMatch(Participation::isActive);
        return new ParticipantStatus(username, total, active);
    }
}
