package ru.vyaly_buhgalter.telegram.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.vyaly_buhgalter.exception.GameAlreadyActiveException;
import ru.vyaly_buhgalter.service.GameSessionService;

@Component
public class StartGameCommand implements BotCommand {

    private static final String COMMAND_NAME = "/startgame";

    private final GameSessionService gameSessionService;

    public StartGameCommand(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public SendMessage execute(Message message) {
        Long chatId = message.getChatId();
        try {
            gameSessionService.startGame(chatId);
            return response(chatId, "🏓 Игра началась! Удачи!");
        } catch (GameAlreadyActiveException e) {
            return response(chatId, "⚠️ Игра уже активна");
        }
    }

    private SendMessage response(Long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
    }
}
