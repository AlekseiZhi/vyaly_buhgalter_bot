package ru.vyaly_buhgalter.exception;

public class NoActiveGameException extends RuntimeException {

    private final Long chatId;

    public NoActiveGameException(Long chatId) {
        super("No active game session found for chatId: " + chatId);
        this.chatId = chatId;
    }

    public Long getChatId() {
        return chatId;
    }
}
