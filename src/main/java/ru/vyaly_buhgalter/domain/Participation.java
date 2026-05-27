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
 * Future extension point: add BigDecimal contribution or hourlyRate
 * for cost calculation once the payment phase is implemented.
 * Use computeDuration(Instant) to get billable time per participant.
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

    protected Participation() {
        // Required by JPA spec
    }

    public static Participation join(GameSession gameSession, Long telegramUserId, String username, Instant now) {
        Participation p = new Participation();
        p.gameSession = gameSession;
        p.telegramUserId = telegramUserId;
        p.username = username;
        p.joinedAt = now;
        return p;
    }

    public Duration leave(Instant now) {
        this.leftAt = now;
        return Duration.between(joinedAt, now);
    }

    public boolean isActive() {
        return leftAt == null;
    }

    public void applyCalculatedCost(BigDecimal cost) {
        this.calculatedCost = cost;
    }

    /**
     * Returns the billable duration of this participation.
     * If still active, calculates against the provided current time.
     */
    public Duration computeDuration(Instant now) {
        return Duration.between(joinedAt, leftAt != null ? leftAt : now);
    }
}
