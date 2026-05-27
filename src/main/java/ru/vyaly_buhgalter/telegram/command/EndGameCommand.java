package ru.vyaly_buhgalter.telegram.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.vyaly_buhgalter.dto.GameCostResult;
import ru.vyaly_buhgalter.service.CostCalculationService;
import ru.vyaly_buhgalter.service.GameSessionService;
import ru.vyaly_buhgalter.telegram.formatter.GameMessageFormatter;
import ru.vyaly_buhgalter.telegram.keyboard.InlineKeyboardFactory;

import java.math.BigDecimal;

@Component
public class EndGameCommand implements BotCommand {

    private static final String COMMAND_NAME = "/endgame";

    private final GameSessionService gameSessionService;
    private final CostCalculationService costCalculationService;
    private final InlineKeyboardFactory keyboardFactory;

    public EndGameCommand(GameSessionService gameSessionService,
                          CostCalculationService costCalculationService,
                          InlineKeyboardFactory keyboardFactory) {
        this.gameSessionService = gameSessionService;
        this.costCalculationService = costCalculationService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public SendMessage execute(Message message) {
        Long chatId = message.getChatId();
        String[] parts = message.getText().trim().split("\\s+");

        if (parts.length < 2) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Укажи общую стоимость за игру: /endgame 150")
                    .replyMarkup(keyboardFactory.buildActiveGameMenu())
                    .build();
        }

        BigDecimal totalCost;
        try {
            totalCost = new BigDecimal(parts[1]);
        } catch (NumberFormatException e) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("⚠️ Неверный формат суммы. Пример: /endgame 150")
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
        return SendMessage.builder()
                .chatId(chatId)
                .text(GameMessageFormatter.formatCostResult(result))
                .replyMarkup(keyboardFactory.buildMainMenu())
                .build();
    }
}
