package ru.vyaly_buhgalter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single game session tied to a Telegram chat.
 *
 * Future extension point: the @OneToMany List<Participation> participants
 * relation is owned by Participation.gameSession — query via ParticipationRepository.
 */
@Entity
@Table(name = "game_sessions")
@Getter
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private Long chatId;

    @Column(nullable = false)
    private Instant startedAt;

    @Column
    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameSessionStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column
    private Long finishedByTelegramUserId;

    @Column
    private String finishedByUsername;

    protected GameSession() {
        // Required by JPA spec
    }

    public static GameSession start(Long chatId, Instant now) {
        GameSession session = new GameSession();
        session.chatId = chatId;
        session.startedAt = now;
        session.status = GameSessionStatus.ACTIVE;
        return session;
    }

    public void finish(Instant now, Long telegramUserId, String username) {
        this.status = GameSessionStatus.FINISHED;
        this.endedAt = now;
        this.finishedByTelegramUserId = telegramUserId;
        this.finishedByUsername = username;
    }

    public void recordCost(BigDecimal cost) {
        this.totalCost = cost;
    }
}
