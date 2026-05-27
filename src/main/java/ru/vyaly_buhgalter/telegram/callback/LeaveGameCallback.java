package ru.vyaly_buhgalter.telegram.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.exception.NoActiveGameException;
import ru.vyaly_buhgalter.exception.NotInGameException;
import ru.vyaly_buhgalter.service.ParticipationService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

import java.time.Duration;

@Slf4j
@Component
public class LeaveGameCallback implements CallbackHandler {

    private final ParticipationService participationService;
    private final InlineKeyboardFactory keyboardFactory;

    public LeaveGameCallback(ParticipationService participationService, InlineKeyboardFactory keyboardFactory) {
        this.participationService = participationService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.LEAVE_GAME.equals(callbackData);
    }

    @Override
    public SendMessage handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Long userId = callbackQuery.getFrom().getId();

        try {
            Duration duration = participationService.leaveGame(chatId, userId);
            log.info("User {} left game in chatId={}, duration={}", userId, chatId, duration);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("✅ Вышел из игры. Время: " + GameMessageFormatter.formatDuration(duration))
                    .replyMarkup(keyboardFactory.buildActiveGameMenu())
                    .build();
        } catch (NoActiveGameException e) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("⚠️ Нет активной игры")
                    .replyMarkup(keyboardFactory.buildMainMenu())
                    .build();
        } catch (NotInGameException e) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("⚠️ Ты не в игре")
                    .replyMarkup(keyboardFactory.buildActiveGameMenu())
                    .build();
        }
    }
}
