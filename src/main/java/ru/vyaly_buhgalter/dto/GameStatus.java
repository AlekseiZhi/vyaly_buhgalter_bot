package ru.vyaly_buhgalter.dto;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Snapshot of an active game session used to render the live game message.
 * All time values are pre-computed by GameStatusService so formatting is pure.
 */
public record GameStatus(
        Instant startedAt,
        Duration gameDuration,
        List<ParticipantStatus> participants
) {
    public record ParticipantStatus(
            String username,
            Duration totalTime,
            boolean active
    ) {}

    public long activeCount() {
        return participants.stream().filter(ParticipantStatus::active).count();
    }
}
