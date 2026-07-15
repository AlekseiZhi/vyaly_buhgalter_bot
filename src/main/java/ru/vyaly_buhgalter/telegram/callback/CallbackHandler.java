package ru.vyaly_buhgalter.telegram.callback;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.io.Serializable;

public interface CallbackHandler {

    boolean supports(String callbackData);

    BotApiMethod<? extends Serializable> handle(CallbackQuery callbackQuery);
}
