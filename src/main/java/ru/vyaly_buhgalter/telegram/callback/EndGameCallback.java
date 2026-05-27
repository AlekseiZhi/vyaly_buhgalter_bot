package ru.vyaly_buhgalter.telegram.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.vyaly_buhgalter.dto.GameCostResult;
import ru.vyaly_buhgalter.service.CostCalculationService;
import ru.vyaly_buhgalter.service.GameSessionService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

import java.math.BigDecimal;

@Slf4j
@Component
public class EndGameCallback implements CallbackHandler {

    private final GameSessionService gameSessionService;
    private final CostCalculationService costCalculationService;
    private final InlineKeyboardFactory keyboardFactory;

    public EndGameCallback(GameSessionService gameSessionService,
                           CostCalculationService costCalculationService,
                           InlineKeyboardFactory keyboardFactory) {
        this.gameSessionService = gameSessionService;
        this.costCalculationService = costCalculationService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(String callbackData) {
        return CallbackData.END_GAME_MENU.equals(callbackData)
                || callbackData.startsWith(CallbackData.END_GAME_PREFIX);
    }

    @Override
    public SendMessage handle(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();

        if (CallbackData.END_GAME_MENU.equals(data)) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("💰 Выбери стоимость корта или введи вручную: /endgame <сумма>")
                    .replyMarkup(keyboardFactory.buildEndGameCostMenu())
                    .build();
        }

        BigDecimal totalCost = parseCost(data);
        if (totalCost == null) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("⚠️ Неверный формат суммы")
                    .replyMarkup(keyboardFactory.buildActiveGameMenu())
                    .build();
        }

        var maybeSession = gameSessionService.finishGame(chatId);
        if (maybeSession.isEmpty()) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("⚠️ Нет активной игры")
                    .replyMarkup(keyboardFactory.buildMainMenu())
                    .build();
        }

        GameCostResult result = costCalculationService.calculateAndSave(maybeSession.get().getId(), totalCost);
        log.info("Game ended via callback for chatId={}, totalCost={}", chatId, totalCost);
        return SendMessage.builder()
                .chatId(chatId)
                .text(GameMessageFormatter.formatCostResult(result))
                .replyMarkup(keyboardFactory.buildMainMenu())
                .build();
    }

    private BigDecimal parseCost(String callbackData) {
        try {
            String raw = callbackData.substring(CallbackData.END_GAME_PREFIX.length());
            return new BigDecimal(raw);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return null;
        }
    }
}
