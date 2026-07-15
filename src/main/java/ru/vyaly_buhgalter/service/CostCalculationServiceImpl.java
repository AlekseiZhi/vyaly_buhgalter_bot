package ru.vyaly_buhgalter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vyaly_buhgalter.domain.GameSession;
import ru.vyaly_buhgalter.domain.Participation;
import ru.vyaly_buhgalter.dto.GameCostResult;
import ru.vyaly_buhgalter.dto.GameCostResult.ParticipantCost;
import ru.vyaly_buhgalter.repository.GameSessionRepository;
import ru.vyaly_buhgalter.repository.ParticipationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CostCalculationServiceImpl implements CostCalculationService {

    private final GameSessionRepository gameSessionRepository;
    private final ParticipationRepository participationRepository;
    private final Clock clock;

    public CostCalculationServiceImpl(GameSessionRepository gameSessionRepository,
                                      ParticipationRepository participationRepository,
                                      Clock clock) {
        this.gameSessionRepository = gameSessionRepository;
        this.participationRepository = participationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public GameCostResult calculateAndSave(UUID gameSessionId, BigDecimal totalCost) {
        GameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalStateException("Session not found: " + gameSessionId));

        Instant endTime = session.getEndedAt() != null ? session.getEndedAt() : Instant.now(clock);

        List<Participation> participations = participationRepository.findAllByGameSessionId(gameSessionId);

        participations.forEach(p -> {
            if (p.isActive()) {
                p.leave(endTime);
            }
        });

        session.recordCost(totalCost);

        if (participations.isEmpty()) {
            log.info("No participants for session {}, recording totalCost only", gameSessionId);
            return new GameCostResult(totalCost, List.of());
        }

        // Group all participation rows by user — a player may have joined/left multiple times.
        Map<Long, List<Participation>> byUser = participations.stream()
                .collect(Collectors.groupingBy(Participation::getTelegramUserId));

        // Sum of all durations across all participations of the same user.
        Map<Long, Duration> durationByUser = byUser.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(p -> p.computeDuration(endTime))
                                .reduce(Duration.ZERO, Duration::plus)
                ));

        Duration totalDuration = durationByUser.values().stream()
                .reduce(Duration.ZERO, Duration::plus);

        // Calculate one cost per unique player.
        Map<Long, BigDecimal> costByUser = totalDuration.isZero()
                ? splitEqually(byUser.keySet().stream().toList(), totalCost)
                : splitProportionally(byUser.keySet().stream().toList(), durationByUser, totalDuration, totalCost);

        // Persist calculatedCost on every participation row.
        byUser.forEach((userId, parts) ->
                applyUserCostToParticipations(parts, costByUser.get(userId), endTime));

        // One result entry per unique player, sorted by cost descending.
        List<ParticipantCost> participantCosts = byUser.entrySet().stream()
                .map(e -> {
                    Long userId = e.getKey();
                    String username = e.getValue().getLast().getUsername();
                    return new ParticipantCost(userId, username, durationByUser.get(userId), costByUser.get(userId));
                })
                .sorted(Comparator.comparing(ParticipantCost::cost).reversed())
                .toList();

        log.info("Costs saved for session {}: {} unique players, total={}",
                gameSessionId, participantCosts.size(), totalCost);
        return new GameCostResult(totalCost, participantCosts);
    }

    // ── cost-splitting helpers ──────────────────────────────────────────────

    private Map<Long, BigDecimal> splitEqually(List<Long> userIds, BigDecimal totalCost) {
        BigDecimal share = totalCost.divide(BigDecimal.valueOf(userIds.size()), 2, RoundingMode.HALF_UP);
        return userIds.stream().collect(Collectors.toMap(Function.identity(), id -> share));
    }

    private Map<Long, BigDecimal> splitProportionally(List<Long> userIds,
                                                       Map<Long, Duration> durationByUser,
                                                       Duration totalDuration,
                                                       BigDecimal totalCost) {
        BigDecimal totalMs = BigDecimal.valueOf(totalDuration.toMillis());
        return userIds.stream().collect(Collectors.toMap(
                Function.identity(),
                id -> {
                    BigDecimal userMs = BigDecimal.valueOf(durationByUser.get(id).toMillis());
                    return totalCost.multiply(userMs).divide(totalMs, 2, RoundingMode.HALF_UP);
                }
        ));
    }

    /**
     * Distributes a user's total cost across their individual participation rows
     * proportionally to each row's duration (so per-row amounts sum to userCost).
     */
    private void applyUserCostToParticipations(List<Participation> parts,
                                                BigDecimal userCost,
                                                Instant endTime) {
        if (parts.size() == 1) {
            parts.getFirst().applyCalculatedCost(userCost);
            return;
        }

        Duration userTotal = parts.stream()
                .map(p -> p.computeDuration(endTime))
                .reduce(Duration.ZERO, Duration::plus);

        if (userTotal.isZero()) {
            BigDecimal share = userCost.divide(BigDecimal.valueOf(parts.size()), 2, RoundingMode.HALF_UP);
            parts.forEach(p -> p.applyCalculatedCost(share));
            return;
        }

        BigDecimal userTotalMs = BigDecimal.valueOf(userTotal.toMillis());
        parts.forEach(p -> {
            BigDecimal partMs = BigDecimal.valueOf(p.computeDuration(endTime).toMillis());
            BigDecimal partCost = userCost.multiply(partMs).divide(userTotalMs, 2, RoundingMode.HALF_UP);
            p.applyCalculatedCost(partCost);
        });
    }
}
