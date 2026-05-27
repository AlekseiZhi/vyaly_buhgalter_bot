package ru.vyaly_buhgalter.telegram.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.vyaly_buhgalter.exception.AlreadyJoinedException;
import ru.vyaly_buhgalter.exception.NoActiveGameException;
import ru.vyaly_buhgalter.service.GameStatusService;
import ru.vyaly_buhgalter.service.ParticipationService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

import java.io.Serializable;

@Slf4j
@Component
public class JoinGameCallback implements CallbackHandler {

    private final ParticipationService participationService;
    private final GameStatusService gameStatusService;
    private final InlineKeyboardFactory keyboardFactory;

    public JoinGameCallback(ParticipationService participationService,
                            GameStatusService gameStatusService,
                            InlineKeyboardFactory keyboardFactory) {
        this.participationService = participationService;
        this.gameStatusService = gameStatusService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.JOIN_GAME.equals(callbackData);
    }

    @Override
    public BotApiMethod<? extends Serializable> handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        User from = callbackQuery.getFrom();
        String username = resolveUsername(from);

        try {
            participationService.joinGame(chatId, from.getId(), username);
            log.info("User {} joined game in chatId={}", username, chatId);
        } catch (NoActiveGameException e) {
            return EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text("⚠️ Сначала начни игру")
                    .replyMarkup(keyboardFactory.buildMainMenu())
                    .build();
        } catch (AlreadyJoinedException ignored) {
            // already in — still refresh the status display
        }

        String text = gameStatusService.getActiveStatus(chatId)
                .map(GameMessageFormatter::formatGameStatus)
                .orElse("⚠️ Игра завершена");

        return EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboardFactory.buildActiveGameMenu())
                .build();
    }

    private String resolveUsername(User user) {
        return user.getUserName() != null ? "@" + user.getUserName() : user.getFirstName();
    }
}
