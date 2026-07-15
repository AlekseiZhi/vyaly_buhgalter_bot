package ru.vyaly_buhgalter.telegram.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.dto.GameStatus;
import ru.vyaly_buhgalter.exception.GameAlreadyActiveException;
import ru.vyaly_buhgalter.service.GameSessionService;
import ru.vyaly_buhgalter.service.GameStatusService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

import java.io.Serializable;

@Slf4j
@Component
public class StartGameCallback implements CallbackHandler {

    private final GameSessionService gameSessionService;
    private final GameStatusService gameStatusService;
    private final InlineKeyboardFactory keyboardFactory;

    public StartGameCallback(GameSessionService gameSessionService,
                             GameStatusService gameStatusService,
                             InlineKeyboardFactory keyboardFactory) {
        this.gameSessionService = gameSessionService;
        this.gameStatusService = gameStatusService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.START_GAME.equals(callbackData);
    }

    @Override
    public BotApiMethod<? extends Serializable> handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        try {
            gameSessionService.startGame(chatId);
            log.info("Game started via callback for chatId={}", chatId);
        } catch (GameAlreadyActiveException ignored) {
            // game already active — fall through to display current status
        }

        String text = gameStatusService.getActiveStatus(chatId)
                .map(GameMessageFormatter::formatGameStatus)
                .orElse("✅ Игра началась!");

        return EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboardFactory.buildActiveGameMenu())
                .build();
    }
}
