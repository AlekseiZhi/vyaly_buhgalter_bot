package ru.vyaly_buhgalter.telegram.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.dto.GameHistoryItem;
import ru.vyaly_buhgalter.service.GameHistoryService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

import java.util.List;

@Component
public class ShowHistoryCallback implements CallbackHandler {

    private static final int HISTORY_LIMIT = 10;

    private final GameHistoryService gameHistoryService;
    private final InlineKeyboardFactory keyboardFactory;

    public ShowHistoryCallback(GameHistoryService gameHistoryService, InlineKeyboardFactory keyboardFactory) {
        this.gameHistoryService = gameHistoryService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.SHOW_HISTORY.equals(callbackData);
    }

    @Override
    public SendMessage handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        List<GameHistoryItem> history = gameHistoryService.getLastGames(chatId, HISTORY_LIMIT);
        return SendMessage.builder()
                .chatId(chatId)
                .text(GameMessageFormatter.formatHistory(history))
                .replyMarkup(keyboardFactory.buildStatsMenu())
                .build();
    }
}
