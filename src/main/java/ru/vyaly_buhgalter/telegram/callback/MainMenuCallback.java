package ru.vyaly_buhgalter.telegram.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.service.GameStatusService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

import java.io.Serializable;
import java.util.Optional;

@Component
public class MainMenuCallback implements CallbackHandler {

    private final GameStatusService gameStatusService;
    private final InlineKeyboardFactory keyboardFactory;

    public MainMenuCallback(GameStatusService gameStatusService, InlineKeyboardFactory keyboardFactory) {
        this.gameStatusService = gameStatusService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.MAIN_MENU.equals(callbackData);
    }

    @Override
    public BotApiMethod<? extends Serializable> handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        var maybeStatus = gameStatusService.getActiveStatus(chatId);

        String text = maybeStatus
                .map(GameMessageFormatter::formatGameStatus)
                .orElse("🏓 Вялый бухгалтер");

        var keyboard = maybeStatus.isPresent()
                ? keyboardFactory.buildActiveGameMenu()
                : keyboardFactory.buildMainMenu();

        return EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard)
                .build();
    }
}
