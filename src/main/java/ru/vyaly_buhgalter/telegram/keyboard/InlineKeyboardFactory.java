package ru.vyaly_buhgalter.telegram.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.vyaly_buhgalter.telegram.callback.CallbackData;

@Component
public class InlineKeyboardFactory {

    /**
     * Shown when there is no active game session.
     */
    public InlineKeyboardMarkup buildMainMenu() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(row(btn("▶️ Начать игру", CallbackData.START_GAME)))
                .build();
    }

    /**
     * Shown while a game session is active.
     */
    public InlineKeyboardMarkup buildActiveGameMenu() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(row(
                        btn("➕ Я в игре", CallbackData.JOIN_GAME),
                        btn("➖ Я вышел", CallbackData.LEAVE_GAME)
                ))
                .keyboardRow(row(
                        btn("📝 Добавить игрока", CallbackData.ADD_PLAYER_HELP),
                        btn("🔄 Обновить", CallbackData.REFRESH_STATUS)
                ))
                .keyboardRow(row(btn("🏁 Завершить игру", CallbackData.END_GAME_MENU)))
                .build();
    }

    /**
     * Navigation menu shown after stats or history output.
     */
    public InlineKeyboardMarkup buildStatsMenu() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(row(
                        btn("🔄 Обновить", CallbackData.SHOW_STATS),
                        btn("📜 История", CallbackData.SHOW_HISTORY)
                ))
                .keyboardRow(row(btn("⬅️ Главное меню", CallbackData.MAIN_MENU)))
                .build();
    }

    /**
     * Preset court-cost buttons shown when ending a game.
     */
    public InlineKeyboardMarkup buildEndGameCostMenu() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(row(
                        btn("50 ₾",  CallbackData.END_GAME_PREFIX + "50"),
                        btn("80 ₾",  CallbackData.END_GAME_PREFIX + "80"),
                        btn("100 ₾", CallbackData.END_GAME_PREFIX + "100")
                ))
                .keyboardRow(row(btn("⬅️ Назад", CallbackData.MAIN_MENU)))
                .build();
    }

    private static InlineKeyboardButton btn(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    private static InlineKeyboardRow row(InlineKeyboardButton... buttons) {
        return new InlineKeyboardRow(buttons);
    }
}
