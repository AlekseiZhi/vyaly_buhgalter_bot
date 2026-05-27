package ru.vyaly_buhgalter.telegram.callback;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackHandler {

    boolean supports(String callbackData);

    SendMessage handle(CallbackQuery callbackQuery);
}
