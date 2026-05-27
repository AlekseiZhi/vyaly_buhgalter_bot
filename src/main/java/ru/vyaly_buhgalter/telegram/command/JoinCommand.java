package ru.vyaly_buhgalter.telegram.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.vyaly_buhgalter.exception.AlreadyJoinedException;
import ru.vyaly_buhgalter.exception.NoActiveGameException;
import ru.vyaly_buhgalter.service.ParticipationService;

@Component
public class JoinCommand implements BotCommand {

    private static final String COMMAND_NAME = "/join";

    private final ParticipationService participationService;

    public JoinCommand(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public SendMessage execute(Message message) {
        Long chatId = message.getChatId();
        User from = message.getFrom();

        try {
            participationService.joinGame(chatId, from.getId(), resolveUsername(from));
            return response(chatId, "Ты вошёл в игру 🏓");
        } catch (NoActiveGameException e) {
            return response(chatId, "⚠️ Нет активной игры. Начни с /startgame");
        } catch (AlreadyJoinedException e) {
            return response(chatId, "⚠️ Ты уже в игре");
        }
    }

    private String resolveUsername(User user) {
        return user.getUserName() != null ? "@" + user.getUserName() : user.getFirstName();
    }

    private SendMessage response(Long chatId, String text) {
        return SendMessage.builder().chatId(chatId).text(text).build();
    }
}
