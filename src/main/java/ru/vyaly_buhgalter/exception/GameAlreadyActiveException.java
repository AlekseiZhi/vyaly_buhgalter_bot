package ru.vyaly_buhgalter.exception;

public class GameAlreadyActiveException extends RuntimeException {

    private final Long chatId;

    public GameAlreadyActiveException(Long chatId) {
        super("Active game session already exists for chatId: " + chatId);
        this.chatId = chatId;
    }

    public Long getChatId() {
        return chatId;
    }
}
