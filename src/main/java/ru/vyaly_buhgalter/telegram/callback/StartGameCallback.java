package ru.vyaly_buhgalter.telegram.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.exception.GameAlreadyActiveException;
import ru.vyaly_buhgalter.service.GameSessionService;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

@Slf4j
@Component
public class StartGameCallback implements CallbackHandler {

    private final GameSessionService gameSessionService;
    private final InlineKeyboardFactory keyboardFactory;

    public StartGameCallback(GameSessionService gameSessionService, InlineKeyboardFactory keyboardFactory) {
        this.gameSessionService = gameSessionService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.START_GAME.equals(callbackData);
    }

    @Override
    public SendMessage handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        try {
            gameSessionService.startGame(chatId);
            log.info("Game started via callback for chatId={}", chatId);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("✅ Игра началась! Нажмите ➕ чтобы войти.")
                    .replyMarkup(keyboardFactory.buildActiveGameMenu())
                    .build();
        } catch (GameAlreadyActiveException e) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("⚠️ Игра уже идёт")
                    .replyMarkup(keyboardFactory.buildActiveGameMenu())
                    .build();
        }
    }
}
