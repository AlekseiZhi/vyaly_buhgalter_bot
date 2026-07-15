package ru.vyaly_buhgalter.telegram.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.vyaly_buhgalter.exception.NoActiveGameException;
import ru.vyaly_buhgalter.service.ParticipationService;

import java.time.Duration;

/**
 * /addplayer @username <time>
 *
 * Manually registers a player who was not present in the chat, identified
 * by their @username. Time can be given as whole minutes (90) or as h:mm (1:30).
 *
 * Examples:
 *   /addplayer @vasya 90
 *   /addplayer @petya 1:30
 *   /addplayer @friend 60
 */
@Component
public class AddPlayerCommand implements BotCommand {

    private static final String COMMAND_NAME = "/addplayer";

    private final ParticipationService participationService;

    public AddPlayerCommand(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public SendMessage execute(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText().strip();
        String[] parts = text.split("\\s+");

        if (parts.length < 3) {
            return response(chatId, """
                    ⚠️ Использование: /addplayer @username <минуты>
                    
                    Примеры:
                    /addplayer @vasya 90
                    /addplayer @petya 1:30""");
        }

        // Everything between the command and the last token is the name.
        String name = normalizeUsername(buildName(parts));
        String timeToken = parts[parts.length - 1];

        Duration duration;
        try {
            duration = parseDuration(timeToken);
        } catch (IllegalArgumentException e) {
            return response(chatId, "⚠️ Неверный формат времени. Используй минуты (90) или ч:мм (1:30)");
        }

        if (duration.isZero() || duration.isNegative()) {
            return response(chatId, "⚠️ Время должно быть больше нуля");
        }

        try {
            participationService.addManualPlayer(chatId, name, duration);
            return response(chatId, String.format(
                    "✅ Игрок %s добавлен вручную (%s)", name, formatDuration(duration)));
        } catch (NoActiveGameException e) {
            return response(chatId, "⚠️ Нет активной игры. Начни с /startgame");
        }
    }

    /** Joins tokens [1 .. len-2] as the player name. */
    private String buildName(String[] parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < parts.length - 1; i++) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(parts[i]);
        }
        return sb.toString().strip();
    }

    /** Ensures the player name is stored as an @username tag. */
    private String normalizeUsername(String name) {
        return name.startsWith("@") ? name : "@" + name;
    }

    /**
     * Accepts "90" (minutes) or "1:30" (hours:minutes).
     */
    private Duration parseDuration(String token) {
        if (token.contains(":")) {
            String[] hm = token.split(":");
            if (hm.length != 2) throw new IllegalArgumentException();
            long hours   = Long.parseLong(hm[0]);
            long minutes = Long.parseLong(hm[1]);
            return Duration.ofMinutes(hours * 60 + minutes);
        }
        return Duration.ofMinutes(Long.parseLong(token));
    }

    private String formatDuration(Duration d) {
        long totalMinutes = d.toMinutes();
        if (totalMinutes < 60) return totalMinutes + " мин";
        return (totalMinutes / 60) + " ч " + (totalMinutes % 60) + " мин";
    }

    private SendMessage response(Long chatId, String text) {
        return SendMessage.builder().chatId(chatId).text(text).build();
    }
}
