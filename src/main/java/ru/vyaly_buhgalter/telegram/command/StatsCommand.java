package ru.vyaly_buhgalter.telegram.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.vyaly_buhgalter.service.StatisticsService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

@Component
public class StatsCommand implements BotCommand {

    private static final String COMMAND_NAME = "/stats";

    private final StatisticsService statisticsService;
    private final InlineKeyboardFactory keyboardFactory;

    public StatsCommand(StatisticsService statisticsService, InlineKeyboardFactory keyboardFactory) {
        this.statisticsService = statisticsService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public SendMessage execute(Message message) {
        Long chatId = message.getChatId();
        return SendMessage.builder()
                .chatId(chatId)
                .text(GameMessageFormatter.formatStats(statisticsService.getPlayerStatistics(chatId)))
                .replyMarkup(keyboardFactory.buildStatsMenu())
                .build();
    }
}
