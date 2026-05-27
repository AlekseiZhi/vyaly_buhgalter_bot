package ru.vyaly_buhgalter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vyaly_buhgalter.domain.GameSessionStatus;
import ru.vyaly_buhgalter.domain.Participation;
import ru.vyaly_buhgalter.dto.PlayerStatistics;
import ru.vyaly_buhgalter.repository.ParticipationRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final ParticipationRepository participationRepository;
    private final Clock clock;

    public StatisticsServiceImpl(ParticipationRepository participationRepository, Clock clock) {
        this.participationRepository = participationRepository;
        this.clock = clock;
    }

    @Override
    public List<PlayerStatistics> getPlayerStatistics(Long chatId) {
        List<Participation> participations = participationRepository
                .findAllByGameSessionChatIdAndStatus(chatId, GameSessionStatus.FINISHED);

        if (participations.isEmpty()) {
            return List.of();
        }

        Instant now = Instant.now(clock);

        Map<Long, List<Participation>> byUser = participations.stream()
                .collect(Collectors.groupingBy(Participation::getTelegramUserId));

        return byUser.entrySet().stream()
                .map(entry -> toStatistics(entry.getKey(), entry.getValue(), now))
                .sorted(Comparator.comparing(PlayerStatistics::totalSpent).reversed())
                .toList();
    }

    private PlayerStatistics toStatistics(Long userId, List<Participation> userParts, Instant now) {
        String username = userParts.getLast().getUsername();

        Duration totalPlayTime = userParts.stream()
                .map(p -> p.computeDuration(now))
                .reduce(Duration.ZERO, Duration::plus);

        BigDecimal totalSpent = userParts.stream()
                .map(Participation::getCalculatedCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int gamesPlayed = userParts.size();

        log.debug("Statistics for user {}: games={}, time={}, spent={}", userId, gamesPlayed, totalPlayTime, totalSpent);
        return new PlayerStatistics(userId, username, totalPlayTime, totalSpent, gamesPlayed);
    }
}
