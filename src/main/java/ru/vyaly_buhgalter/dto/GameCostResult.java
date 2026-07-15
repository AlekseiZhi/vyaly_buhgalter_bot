package ru.vyaly_buhgalter.dto;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * Immutable result of a game cost calculation.
 * Contains per-participant breakdown and the total.
 *
 * Designed for direct use in Telegram message formatting
 * and future persistence (billing history).
 */
public record GameCostResult(
        BigDecimal totalCost,
        List<ParticipantCost> participants
) {

    public boolean hasParticipants() {
        return !participants.isEmpty();
    }

    /**
     * Per-participant cost entry.
     * duration is the billable time for this participant.
     */
    public record ParticipantCost(
            Long telegramUserId,
            String username,
            Duration duration,
            BigDecimal cost
    ) {}
}
