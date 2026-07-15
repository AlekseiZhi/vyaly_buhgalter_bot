package ru.vyaly_buhgalter.service;

import ru.vyaly_buhgalter.dto.PlayerStatistics;

import java.util.List;

public interface StatisticsService {

    /**
     * Returns aggregated statistics for every player who participated
     * in a finished game within the given chat. Sorted by total spent descending.
     */
    List<PlayerStatistics> getPlayerStatistics(Long chatId);
}
