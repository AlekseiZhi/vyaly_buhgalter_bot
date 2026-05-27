package ru.vyaly_buhgalter.service;

import ru.vyaly_buhgalter.dto.GameHistoryItem;

import java.util.List;

public interface GameHistoryService {

    /**
     * Returns the most recent finished games for the given chat, newest first.
     */
    List<GameHistoryItem> getLastGames(Long chatId, int limit);
}
