package ru.vyaly_buhgalter.telegram.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.dto.PlayerStatistics;
import ru.vyaly_buhgalter.service.StatisticsService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

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
    public SendMessage handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        List<PlayerStatistics> stats = statisticsService.getPlayerStatistics(chatId);
        return SendMessage.builder()
                .chatId(chatId)
                .text(GameMessageFormatter.formatStats(stats))
                .replyMarkup(keyboardFactory.buildStatsMenu())
                .build();
    }
}
