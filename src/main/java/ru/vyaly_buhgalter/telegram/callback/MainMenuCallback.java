package ru.vyaly_buhgalter.telegram.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.service.GameSessionService;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

@Component
public class MainMenuCallback implements CallbackHandler {

    private final GameSessionService gameSessionService;
    private final InlineKeyboardFactory keyboardFactory;

    public MainMenuCallback(GameSessionService gameSessionService, InlineKeyboardFactory keyboardFactory) {
        this.gameSessionService = gameSessionService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.MAIN_MENU.equals(callbackData);
    }

    @Override
    public SendMessage handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        boolean hasActiveGame = gameSessionService.getActiveGame(chatId).isPresent();
        return SendMessage.builder()
                .chatId(chatId)
                .text("🏓 Вялый бухгалтер")
                .replyMarkup(hasActiveGame
                        ? keyboardFactory.buildActiveGameMenu()
                        : keyboardFactory.buildMainMenu())
                .build();
    }
}
