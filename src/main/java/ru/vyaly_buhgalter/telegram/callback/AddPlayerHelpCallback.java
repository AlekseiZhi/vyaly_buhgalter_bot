package ru.vyaly_buhgalter.telegram.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

import java.io.Serializable;

@Component
public class AddPlayerHelpCallback implements CallbackHandler {

    private final InlineKeyboardFactory keyboardFactory;

    public AddPlayerHelpCallback(InlineKeyboardFactory keyboardFactory) {
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.ADD_PLAYER_HELP.equals(callbackData);
    }

    @Override
    public BotApiMethod<? extends Serializable> handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        String text = """
                📝 Добавление игрока вручную
                
                Используй команду в чате:
                /addplayer @username <минуты>
                
                Примеры:
                /addplayer @vasya 90
                /addplayer @petya 1:30
                /addplayer @friend 60
                
                Формат времени: минуты (90) или часы:минуты (1:30)
                """.stripTrailing();

        return EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboardFactory.buildActiveGameMenu())
                .build();
    }
}
