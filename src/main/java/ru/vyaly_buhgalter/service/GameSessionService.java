package ru.vyaly_buhgalter.service;

import ru.vyaly_buhgalter.domain.GameSession;

import java.util.Optional;

public interface GameSessionService {

    GameSession startGame(Long chatId);

    Optional<GameSession> getActiveGame(Long chatId);

    Optional<GameSession> finishGame(Long chatId);
}
