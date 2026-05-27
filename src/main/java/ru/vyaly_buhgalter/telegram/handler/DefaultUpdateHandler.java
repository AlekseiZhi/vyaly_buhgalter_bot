package ru.vyaly_buhgalter.telegram.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.vyaly_buhgalter.telegram.callback.CallbackRouter;
import ru.vyaly_buhgalter.telegram.command.BotCommand;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DefaultUpdateHandler implements UpdateHandler {

    private final TelegramClient telegramClient;
    private final Map<String, BotCommand> commandRegistry;
    private final CallbackRouter callbackRouter;

    public DefaultUpdateHandler(TelegramClient telegramClient,
                                List<BotCommand> commands,
                                CallbackRouter callbackRouter) {
        this.telegramClient = telegramClient;
        this.commandRegistry = commands.stream()
                .collect(Collectors.toMap(BotCommand::getCommandName, Function.identity()));
        this.callbackRouter = callbackRouter;
        log.info("Registered commands: {}", commandRegistry.keySet());
    }

    @Override
    public void handle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleMessage(Message message) {
        String text = message.getText().trim();
        log.debug("Received text: '{}' from chatId: {}", text, message.getChatId());

        if (!text.startsWith("/")) {
            return;
        }

        String commandKey = text.split("\\s+")[0].split("@")[0].toLowerCase();
        BotCommand command = commandRegistry.get(commandKey);

        if (command == null) {
            log.debug("Unknown command: '{}'", commandKey);
            return;
        }

        send(command.execute(message));
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        callbackRouter.route(callbackQuery).ifPresent(this::send);
        answerCallback(callbackQuery.getId());
    }

    private void answerCallback(String callbackQueryId) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback query id={}: {}", callbackQueryId, e.getMessage(), e);
        }
    }

    private void send(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId {}: {}", message.getChatId(), e.getMessage(), e);
        }
    }
}
