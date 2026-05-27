package ru.vyaly_buhgalter.exception;

public class NotInGameException extends RuntimeException {

    private final Long telegramUserId;

    public NotInGameException(Long telegramUserId) {
        super("No active participation found for telegramUserId: " + telegramUserId);
        this.telegramUserId = telegramUserId;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }
}
