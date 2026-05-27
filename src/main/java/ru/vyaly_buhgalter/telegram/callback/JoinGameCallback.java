package ru.vyaly_buhgalter.telegram.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.vyaly_buhgalter.exception.AlreadyJoinedException;
import ru.vyaly_buhgalter.exception.NoActiveGameException;
import ru.vyaly_buhgalter.service.ParticipationService;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

@Slf4j
@Component
public class JoinGameCallback implements CallbackHandler {

    private final ParticipationService participationService;
    private final InlineKeyboardFactory keyboardFactory;

    public JoinGameCallback(ParticipationService participationService, InlineKeyboardFactory keyboardFactory) {
        this.participationService = participationService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.JOIN_GAME.equals(callbackData);
    }

    @Override
    public SendMessage handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        User from = callbackQuery.getFrom();
        String username = resolveUsername(from);

        try {
            participationService.joinGame(chatId, from.getId(), username);
            log.info("User {} joined game in chatId={}", username, chatId);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("✅ " + username + " вошёл в игру 🏓")
                    .replyMarkup(keyboardFactory.buildActiveGameMenu())
                    .build();
        } catch (NoActiveGameException e) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("⚠️ Сначала начни игру")
                    .replyMarkup(keyboardFactory.buildMainMenu())
                    .build();
        } catch (AlreadyJoinedException e) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("ℹ️ Ты уже в игре")
                    .replyMarkup(keyboardFactory.buildActiveGameMenu())
                    .build();
        }
    }

    private String resolveUsername(User user) {
        return user.getUserName() != null ? "@" + user.getUserName() : user.getFirstName();
    }
}
