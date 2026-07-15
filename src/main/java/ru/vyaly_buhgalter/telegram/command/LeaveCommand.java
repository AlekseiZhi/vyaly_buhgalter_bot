package ru.vyaly_buhgalter.telegram.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.vyaly_buhgalter.exception.NoActiveGameException;
import ru.vyaly_buhgalter.exception.NotInGameException;
import ru.vyaly_buhgalter.service.ParticipationService;

import java.time.Duration;

@Component
public class LeaveCommand implements BotCommand {

    private static final String COMMAND_NAME = "/leave";

    private final ParticipationService participationService;

    public LeaveCommand(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public SendMessage execute(Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        try {
            Duration duration = participationService.leaveGame(chatId, userId);
            return response(chatId, "Ты вышел из игры. Время: " + formatDuration(duration));
        } catch (NoActiveGameException e) {
            return response(chatId, "⚠️ Нет активной игры");
        } catch (NotInGameException e) {
            return response(chatId, "⚠️ Ты не в игре");
        }
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return hours > 0
                ? "%dч %dмин".formatted(hours, minutes)
                : "%dмин".formatted(minutes);
    }

    private SendMessage response(Long chatId, String text) {
        return SendMessage.builder().chatId(chatId).text(text).build();
    }
}
