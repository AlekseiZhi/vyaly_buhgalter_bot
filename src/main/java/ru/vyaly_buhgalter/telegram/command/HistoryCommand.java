package ru.vyaly_buhgalter.telegram.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.vyaly_buhgalter.service.GameHistoryService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

@Component
public class HistoryCommand implements BotCommand {

    private static final String COMMAND_NAME = "/history";
    private static final int HISTORY_LIMIT = 10;

    private final GameHistoryService gameHistoryService;
    private final InlineKeyboardFactory keyboardFactory;

    public HistoryCommand(GameHistoryService gameHistoryService, InlineKeyboardFactory keyboardFactory) {
        this.gameHistoryService = gameHistoryService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public SendMessage execute(Message message) {
        Long chatId = message.getChatId();
        return SendMessage.builder()
                .chatId(chatId)
                .text(GameMessageFormatter.formatHistory(gameHistoryService.getLastGames(chatId, HISTORY_LIMIT)))
                .replyMarkup(keyboardFactory.buildStatsMenu())
                .build();
    }
}
