package ru.vyaly_buhgalter.telegram.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class CallbackRouter {

    private final List<CallbackHandler> handlers;

    public CallbackRouter(List<CallbackHandler> handlers) {
        this.handlers = handlers;
        log.info("Registered callback handlers: {}",
                handlers.stream().map(h -> h.getClass().getSimpleName()).toList());
    }

    public Optional<BotApiMethod<? extends Serializable>> route(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (data == null || data.isBlank()) {
            log.warn("Received callback query with blank data, id={}", callbackQuery.getId());
            return Optional.empty();
        }

        return handlers.stream()
                .filter(h -> h.supports(data))
                .findFirst()
                .map(h -> {
                    log.debug("Routing callback '{}' to {}", data, h.getClass().getSimpleName());
                    return h.handle(callbackQuery);
                });
    }
}
