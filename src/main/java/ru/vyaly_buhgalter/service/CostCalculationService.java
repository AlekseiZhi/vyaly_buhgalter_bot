package ru.vyaly_buhgalter.service;

import ru.vyaly_buhgalter.dto.GameCostResult;

import java.math.BigDecimal;
import java.util.UUID;

public interface CostCalculationService {

    /**
     * Calculates per-participant costs, persists calculatedCost on each Participation,
     * records totalCost on GameSession, and closes any still-active participations
     * using the session's endedAt as the effective leave time.
     */
    GameCostResult calculateAndSave(UUID gameSessionId, BigDecimal totalCost);
}
