package ru.vyaly_buhgalter.telegram.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.vyaly_buhgalter.domain.GameSession;
import ru.vyaly_buhgalter.service.GameSessionService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class StatusCommand implements BotCommand {

    private static final String COMMAND_NAME = "/status";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy").withZone(ZoneId.of("Asia/Tbilisi"));

    private final GameSessionService gameSessionService;
    private final Clock clock;

    public StatusCommand(GameSessionService gameSessionService, Clock clock) {
        this.gameSessionService = gameSessionService;
        this.clock = clock;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public SendMessage execute(Message message) {
        Long chatId = message.getChatId();
        return gameSessionService.getActiveGame(chatId)
                .map(session -> response(chatId, formatActiveSession(session)))
                .orElseGet(() -> response(chatId, "Сейчас активных игр нет"));
    }

    private String formatActiveSession(GameSession session) {
        return """
                🏓 Активная игра
                ⏱ Начата: %s
                ⌛ Длительность: %s""".formatted(
                DATE_FORMATTER.format(session.getStartedAt()),
                formatDuration(session.getStartedAt())
        );
    }

    private String formatDuration(Instant startedAt) {
        Duration duration = Duration.between(startedAt, Instant.now(clock));
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return hours > 0
                ? "%dч %dмин".formatted(hours, minutes)
                : "%dмин".formatted(minutes);
    }

    private SendMessage response(Long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
    }
}
