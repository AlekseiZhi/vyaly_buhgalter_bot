package ru.vyaly_buhgalter.telegram.formatter;

import ru.vyaly_buhgalter.dto.GameCostResult;
import ru.vyaly_buhgalter.dto.GameHistoryItem;
import ru.vyaly_buhgalter.dto.GameStatus;
import ru.vyaly_buhgalter.dto.GameStatus.ParticipantStatus;
import ru.vyaly_buhgalter.dto.PlayerStatistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pure formatting utilities for all outgoing Telegram messages.
 * No Spring beans, no service calls — only text assembly.
 */
public final class GameMessageFormatter {

    private static final ZoneId ZONE = ZoneId.of("Asia/Tbilisi");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZONE);
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZONE);
    private static final DateTimeFormatter TIME_SEC_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZONE);

    private GameMessageFormatter() {}

    public static String formatGameStatus(GameStatus status) {
        List<ParticipantStatus> active = status.participants().stream()
                .filter(ParticipantStatus::active).toList();
        List<ParticipantStatus> left = status.participants().stream()
                .filter(p -> !p.active()).toList();

        StringBuilder sb = new StringBuilder();
        sb.append("🏓 Игра идёт | Начало: ").append(TIME_FMT.format(status.startedAt()));
        sb.append(" | ").append(formatDuration(status.gameDuration())).append("\n");

        if (status.participants().isEmpty()) {
            sb.append("\nУчастников пока нет — нажмите ➕");
        } else {
            if (!active.isEmpty()) {
                sb.append("\n▶️ В игре (").append(active.size()).append("):\n");
                active.forEach(p -> sb.append("  • ").append(p.username())
                        .append(" — ").append(formatDuration(p.totalTime())).append("\n"));
            }
            if (!left.isEmpty()) {
                sb.append("\n⏹ Вышли (").append(left.size()).append("):\n");
                left.forEach(p -> sb.append("  • ").append(p.username())
                        .append(" — ").append(formatDuration(p.totalTime())).append("\n"));
            }
        }
        // Current moment derived from startedAt + elapsed; gives a "live" feel and
        // guarantees content changes on refresh (avoids "message is not modified").
        sb.append("\n\n🔄 обновлено в ")
                .append(TIME_SEC_FMT.format(status.startedAt().plus(status.gameDuration())));
        return sb.toString().stripTrailing();
    }

    public static String formatStats(List<PlayerStatistics> stats) {
        if (stats.isEmpty()) {
            return "📊 Статистика пока пуста — сыграйте первую игру!";
        }
        StringBuilder sb = new StringBuilder("📊 Статистика игроков\n");
        stats.forEach(s -> {
            sb.append("\n@").append(s.username()).append(":\n");
            sb.append("⏱ ").append(formatDuration(s.totalPlayTime())).append("\n");
            sb.append("💰 ").append(formatMoney(s.totalSpent())).append(" ₾\n");
            sb.append("🎾 ").append(s.gamesPlayed()).append(" ").append(gamesWord(s.gamesPlayed())).append("\n");
        });
        return sb.toString().stripTrailing();
    }

    public static String formatHistory(List<GameHistoryItem> history) {
        if (history.isEmpty()) {
            return "🏓 История игр пуста — сыграйте первую игру!";
        }
        StringBuilder sb = new StringBuilder("🏓 Последние игры\n\n");
        for (int i = 0; i < history.size(); i++) {
            GameHistoryItem item = history.get(i);
            String date = DATE_FMT.format(item.startedAt());
            String cost = item.totalCost() != null ? formatMoney(item.totalCost()) + " ₾" : "—";
            String duration = formatDuration(item.totalDuration());
            String players = item.playersCount() + " чел.";
            sb.append("%d. %s — %s — %s — %s\n".formatted(i + 1, date, cost, duration, players));
        }
        return sb.toString().stripTrailing();
    }

    public static String formatCostResult(GameCostResult result) {
        if (!result.hasParticipants()) {
            return "🏓 Игра завершена. Участников не было.";
        }
        StringBuilder sb = new StringBuilder("🏓 Итоги игры\n\n");
        result.participants().forEach(pc ->
                sb.append("%s — %s — %s ₾\n".formatted(
                        pc.username(), formatDuration(pc.duration()), formatMoney(pc.cost())))
        );
        sb.append("\nВсего: %s ₾".formatted(formatMoney(result.totalCost())));
        return sb.toString();
    }

    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        if (hours > 0) {
            return "%dч %dмин".formatted(hours, minutes);
        }
        return "%dмин".formatted(minutes);
    }

    public static String formatMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String gamesWord(int count) {
        int mod10 = count % 10;
        int mod100 = count % 100;
        if (mod10 == 1 && mod100 != 11) return "игра";
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return "игры";
        return "игр";
    }
}
