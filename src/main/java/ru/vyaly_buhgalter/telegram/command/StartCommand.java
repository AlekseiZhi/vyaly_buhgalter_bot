package ru.vyaly_buhgalter.telegram.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.vyaly_buhgalter.service.GameSessionService;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

@Component
public class StartCommand implements BotCommand {

    private static final String COMMAND_NAME = "/start";

    private final GameSessionService gameSessionService;
    private final InlineKeyboardFactory keyboardFactory;

    public StartCommand(GameSessionService gameSessionService, InlineKeyboardFactory keyboardFactory) {
        this.gameSessionService = gameSessionService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public SendMessage execute(Message message) {
        Long chatId = message.getChatId();
        boolean hasActiveGame = gameSessionService.getActiveGame(chatId).isPresent();
        return SendMessage.builder()
                .chatId(chatId)
                .text("🏓 Вялый бухгалтер работает")
                .replyMarkup(hasActiveGame
                        ? keyboardFactory.buildActiveGameMenu()
                        : keyboardFactory.buildMainMenu())
                .build();
    }
}
