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
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

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

        List<Duration> durations = participations.stream()
                .map(p -> p.computeDuration(endTime))
                .toList();

        Duration totalDuration = durations.stream().reduce(Duration.ZERO, Duration::plus);

        List<ParticipantCost> participantCosts = totalDuration.isZero()
                ? splitEqually(participations, durations, totalCost)
                : splitProportionally(participations, durations, totalDuration, totalCost);

        for (int i = 0; i < participations.size(); i++) {
            participations.get(i).applyCalculatedCost(participantCosts.get(i).cost());
        }

        log.info("Costs calculated and saved for session {}: {} participants, total={}",
                gameSessionId, participantCosts.size(), totalCost);
        return new GameCostResult(totalCost, participantCosts);
    }

    private List<ParticipantCost> splitEqually(
            List<Participation> participations,
            List<Duration> durations,
            BigDecimal totalCost) {
        BigDecimal share = totalCost.divide(BigDecimal.valueOf(participations.size()), 2, RoundingMode.HALF_UP);
        return buildResults(participations, durations, (p, d) -> share);
    }

    private List<ParticipantCost> splitProportionally(
            List<Participation> participations,
            List<Duration> durations,
            Duration totalDuration,
            BigDecimal totalCost) {
        BigDecimal totalMs = BigDecimal.valueOf(totalDuration.toMillis());
        return buildResults(participations, durations, (p, duration) -> {
            BigDecimal userMs = BigDecimal.valueOf(duration.toMillis());
            return totalCost.multiply(userMs).divide(totalMs, 2, RoundingMode.HALF_UP);
        });
    }

    @FunctionalInterface
    private interface CostFunction {
        BigDecimal apply(Participation participation, Duration duration);
    }

    private List<ParticipantCost> buildResults(
            List<Participation> participations,
            List<Duration> durations,
            CostFunction costFn) {
        return IntStream.range(0, participations.size())
                .mapToObj(i -> {
                    Participation p = participations.get(i);
                    Duration d = durations.get(i);
                    return new ParticipantCost(p.getTelegramUserId(), p.getUsername(), d, costFn.apply(p, d));
                })
                .toList();
    }
}
