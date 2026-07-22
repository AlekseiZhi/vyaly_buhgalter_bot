package ru.vyaly_buhgalter.exception;

public class GameFinishForbiddenException extends RuntimeException {

    public GameFinishForbiddenException(Long telegramUserId) {
        super("User is not a participant of the active game: " + telegramUserId);
    }
}
