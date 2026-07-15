package ru.vyaly_buhgalter.exception;

public class AlreadyJoinedException extends RuntimeException {

    private final Long telegramUserId;

    public AlreadyJoinedException(Long telegramUserId) {
        super("User already has an active participation: telegramUserId=" + telegramUserId);
        this.telegramUserId = telegramUserId;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }
}
