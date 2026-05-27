package ru.vyaly_buhgalter.dto;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

public record GameHistoryItem(
        Instant startedAt,
        BigDecimal totalCost,
        Duration totalDuration,
        int playersCount
) {}
