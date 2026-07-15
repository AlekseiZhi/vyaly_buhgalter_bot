package ru.vyaly_buhgalter.telegram.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.dto.PlayerStatistics;
import ru.vyaly_buhgalter.service.StatisticsService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

import java.io.Serializable;
import java.util.List;

@Component
public class ShowStatsCallback implements CallbackHandler {

    private final StatisticsService statisticsService;
    private final InlineKeyboardFactory keyboardFactory;

    public ShowStatsCallback(StatisticsService statisticsService, InlineKeyboardFactory keyboardFactory) {
        this.statisticsService = statisticsService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.SHOW_STATS.equals(callbackData);
    }

    @Override
    public BotApiMethod<? extends Serializable> handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        List<PlayerStatistics> stats = statisticsService.getPlayerStatistics(chatId);
        // Send as a new message so the game message stays visible in chat.
        return SendMessage.builder()
                .chatId(chatId)
                .text(GameMessageFormatter.formatStats(stats))
                .replyMarkup(keyboardFactory.buildStatsMenu())
                .build();
    }
}
