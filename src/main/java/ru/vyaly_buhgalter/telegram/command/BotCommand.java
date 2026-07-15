package ru.vyaly_buhgalter.telegram.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface BotCommand {

    String getCommandName();

    SendMessage execute(Message message);
}
