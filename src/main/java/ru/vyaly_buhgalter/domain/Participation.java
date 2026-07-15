package ru.vyaly_buhgalter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * Tracks a single user's presence in a GameSession.
 *
 * Every player is identified by a Telegram account. Manual entries
 * (added by the organiser via @username for players who missed the chat
 * or forgot to press "join") get a deterministic synthetic id derived
 * from their @username, so they merge across games for statistics.
 * manualDurationSeconds overrides time-based calculation when set.
 */
@Entity
@Table(name = "participations")
@Getter
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    @Column(nullable = false)
    private Long telegramUserId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Instant joinedAt;

    @Column
    private Instant leftAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal calculatedCost;

    /** Non-null only for manual entries; overrides joinedAt/leftAt duration. */
    @Column
    private Long manualDurationSeconds;

    protected Participation() {}

    public static Participation join(GameSession gameSession, Long telegramUserId, String username, Instant now) {
        Participation p = new Participation();
        p.gameSession = gameSession;
        p.telegramUserId = telegramUserId;
        p.username = username;
        p.joinedAt = now;
        return p;
    }

    /**
     * Creates a participation record for a player who was not in the chat,
     * identified by their @username. The id is derived deterministically from
     * the username so repeated manual entries of the same person merge for stats.
     * The duration is fixed and does not depend on join/leave timestamps.
     */
    public static Participation manual(GameSession gameSession, String username, Duration duration, Instant now) {
        Participation p = new Participation();
        p.gameSession = gameSession;
        p.username = username;
        p.telegramUserId = syntheticId(username);
        p.joinedAt = now;
        p.leftAt = now;
        p.manualDurationSeconds = duration.toSeconds();
        return p;
    }

    /**
     * Builds a stable, negative id from a @username. Real Telegram ids are
     * positive, so negative values never collide with genuine accounts.
     */
    public static long syntheticId(String username) {
        long hash = username.toLowerCase().strip().hashCode() & 0xFFFFFFFFL;
        return -(hash + 1);
    }

    public Duration leave(Instant now) {
        this.leftAt = now;
        return Duration.between(joinedAt, now);
    }

    public boolean isActive() {
        return leftAt == null;
    }

    public boolean isManual() {
        return manualDurationSeconds != null;
    }

    public void applyCalculatedCost(BigDecimal cost) {
        this.calculatedCost = cost;
    }

    /**
     * Returns the billable duration.
     * For manual entries returns the fixed pre-set duration.
     * For regular entries uses timestamps (leftAt or current time if still active).
     */
    public Duration computeDuration(Instant now) {
        if (manualDurationSeconds != null) {
            return Duration.ofSeconds(manualDurationSeconds);
        }
        return Duration.between(joinedAt, leftAt != null ? leftAt : now);
    }
}
