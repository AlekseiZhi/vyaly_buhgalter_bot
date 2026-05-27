package ru.vyaly_buhgalter.service;

import ru.vyaly_buhgalter.domain.Participation;

import java.time.Duration;

public interface ParticipationService {

    Participation joinGame(Long chatId, Long telegramUserId, String username);

    Duration leaveGame(Long chatId, Long telegramUserId);
}
