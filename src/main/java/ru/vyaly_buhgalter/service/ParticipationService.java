package ru.vyaly_buhgalter.service;

import ru.vyaly_buhgalter.domain.Participation;

import java.time.Duration;

public interface ParticipationService {

    Participation joinGame(Long chatId, Long telegramUserId, String username);

    Duration leaveGame(Long chatId, Long telegramUserId);

    /**
     * Manually registers a player who was not present in the Telegram chat.
     * The given duration is used directly for cost calculation.
     */
    Participation addManualPlayer(Long chatId, String username, Duration duration);
}
