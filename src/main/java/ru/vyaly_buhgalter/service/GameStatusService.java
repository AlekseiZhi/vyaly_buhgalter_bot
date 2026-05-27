package ru.vyaly_buhgalter.service;

import ru.vyaly_buhgalter.dto.GameStatus;

import java.util.Optional;

public interface GameStatusService {

    /**
     * Returns a pre-computed status snapshot of the currently active game
     * for the given chat, or empty if no game is running.
     */
    Optional<GameStatus> getActiveStatus(Long chatId);
}
