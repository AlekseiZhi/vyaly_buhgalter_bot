package ru.vyaly_buhgalter.telegram.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.service.GameStatusService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

import java.io.Serializable;

/**
 * Re-renders the active game status without changing any state,
 * letting players refresh "who is playing and for how long" on demand.
 */
@Component
public class RefreshStatusCallback implements CallbackHandler {

    private final GameStatusService gameStatusService;
    private final InlineKeyboardFactory keyboardFactory;

    public RefreshStatusCallback(GameStatusService gameStatusService,
                                 InlineKeyboardFactory keyboardFactory) {
        this.gameStatusService = gameStatusService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.REFRESH_STATUS.equals(callbackData);
    }

    @Override
    public BotApiMethod<? extends Serializable> handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        var status = gameStatusService.getActiveStatus(chatId);
        if (status.isPresent()) {
            return EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(GameMessageFormatter.formatGameStatus(status.get()))
                    .replyMarkup(keyboardFactory.buildActiveGameMenu())
                    .build();
        }
        return EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text("⚠️ Активной игры нет")
                .replyMarkup(keyboardFactory.buildMainMenu())
                .build();
    }
}
