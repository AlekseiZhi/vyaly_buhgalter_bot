package ru.vyaly_buhgalter.telegram.callback;

/**
 * String constants for all inline keyboard callback_data values.
 *
 * Convention:
 *  - simple actions are plain strings (e.g. "START_GAME")
 *  - parameterized actions use "PREFIX:value" format (e.g. "END_GAME:150")
 */
public final class CallbackData {

    public static final String START_GAME    = "START_GAME";
    public static final String JOIN_GAME     = "JOIN_GAME";
    public static final String LEAVE_GAME    = "LEAVE_GAME";
    public static final String END_GAME_MENU = "END_GAME_MENU";
    public static final String END_GAME_PREFIX = "END_GAME:";
    public static final String SHOW_STATS    = "SHOW_STATS";
    public static final String SHOW_HISTORY  = "SHOW_HISTORY";
    public static final String MAIN_MENU        = "MAIN_MENU";
    public static final String ADD_PLAYER_HELP  = "ADD_PLAYER_HELP";
    public static final String REFRESH_STATUS   = "REFRESH_STATUS";

    private CallbackData() {}
}
