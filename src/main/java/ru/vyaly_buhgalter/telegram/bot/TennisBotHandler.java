package ru.vyaly_buhgalter.telegram.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.vyaly_buhgalter.config.BotProperties;
import ru.vyaly_buhgalter.telegram.handler.UpdateHandler;

@Slf4j
@Component
public class TennisBotHandler implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final BotProperties botProperties;
    private final UpdateHandler updateHandler;

    public TennisBotHandler(BotProperties botProperties, UpdateHandler updateHandler) {
        this.botProperties = botProperties;
        this.updateHandler = updateHandler;
    }

    @Override
    public String getBotToken() {
        return botProperties.token();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        try {
            updateHandler.handle(update);
        } catch (Exception e) {
            log.error("Unhandled exception while processing update id={}: {}", update.getUpdateId(), e.getMessage(), e);
        }
    }

    @AfterBotRegistration
    public void onRegistration(BotSession botSession) {
        log.info("Bot @{} registered, running: {}", botProperties.username(), botSession.isRunning());
    }
}
