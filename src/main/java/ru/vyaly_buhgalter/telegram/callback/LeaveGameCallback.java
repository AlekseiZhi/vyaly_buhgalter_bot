package ru.vyaly_buhgalter.telegram.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.exception.NoActiveGameException;
import ru.vyaly_buhgalter.exception.NotInGameException;
import ru.vyaly_buhgalter.service.GameStatusService;
import ru.vyaly_buhgalter.service.ParticipationService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

import java.io.Serializable;

@Slf4j
@Component
public class LeaveGameCallback implements CallbackHandler {

    private final ParticipationService participationService;
    private final GameStatusService gameStatusService;
    private final InlineKeyboardFactory keyboardFactory;

    public LeaveGameCallback(ParticipationService participationService,
                             GameStatusService gameStatusService,
                             InlineKeyboardFactory keyboardFactory) {
        this.participationService = participationService;
        this.gameStatusService = gameStatusService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.LEAVE_GAME.equals(callbackData);
    }

    @Override
    public BotApiMethod<? extends Serializable> handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long userId = callbackQuery.getFrom().getId();

        try {
            participationService.leaveGame(chatId, userId);
            log.info("User {} left game in chatId={}", userId, chatId);
        } catch (NoActiveGameException e) {
            return EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text("⚠️ Нет активной игры")
                    .replyMarkup(keyboardFactory.buildMainMenu())
                    .build();
        } catch (NotInGameException ignored) {
            // not in game — still refresh the status display
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
}
