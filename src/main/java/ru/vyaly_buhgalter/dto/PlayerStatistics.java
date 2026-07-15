package ru.vyaly_buhgalter.dto;

import java.math.BigDecimal;
import java.time.Duration;

public record PlayerStatistics(
        Long telegramUserId,
        String username,
        Duration totalPlayTime,
        BigDecimal totalSpent,
        int gamesPlayed
) {}
