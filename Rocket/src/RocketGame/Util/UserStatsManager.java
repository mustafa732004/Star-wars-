package RocketGame.Util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserStatsManager {

    private static final String STATS_FILE = "player_stats.txt";
    private final Map<String, PlayerStats> playerStatsMap;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dateTimeFormat;

    public UserStatsManager() {
        this.playerStatsMap = new HashMap<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        loadStats();
    }

public static class PlayerStats {
        public String username;
        public int highScore;
        public int timesPlayed;
        public long totalPlayTime;
        public String lastPlayDate;
        public int gamesWon;
        public String lastGameTime;
        public String lastTimePlayed;

        public PlayerStats(String username, int highScore, int timesPlayed,
                           long totalPlayTime, String lastPlayDate, int gamesWon,
                           String lastGameTime, String lastTimePlayed) {
            this.username = username;
            this.highScore = highScore;
            this.timesPlayed = timesPlayed;
            this.totalPlayTime = totalPlayTime;
            this.lastPlayDate = lastPlayDate;
            this.gamesWon = gamesWon;
            this.lastGameTime = lastGameTime;
            this.lastTimePlayed = lastTimePlayed;
        }

        @Override
        public String toString() {
            return username + "|" + highScore + "|" + timesPlayed + "|" +
                    totalPlayTime + "|" + lastPlayDate + "|" + gamesWon + "|" +
                    lastGameTime + "|" + lastTimePlayed;
        }
    }

    public void updatePlayerStats(String username, int score, long playTimeSeconds, boolean isVictory) {
        String currentDate = dateFormat.format(new Date());
        String currentDateTime = dateTimeFormat.format(new Date());


        String lastGameTime = formatGameTime(playTimeSeconds);

        if (playerStatsMap.containsKey(username)) {
            PlayerStats stats = playerStatsMap.get(username);
            stats.highScore = Math.max(stats.highScore, score);
            stats.timesPlayed++;
            stats.totalPlayTime += playTimeSeconds;
            stats.lastPlayDate = currentDate;
            stats.lastGameTime = lastGameTime;
            stats.lastTimePlayed = currentDateTime;
            if (isVictory) {
                stats.gamesWon++;
            }
        } else {

            int gamesWon = isVictory ? 1 : 0;
            PlayerStats newStats = new PlayerStats(username, score, 1, playTimeSeconds,
                    currentDate, gamesWon, lastGameTime, currentDateTime);
            playerStatsMap.put(username, newStats);
        }

        saveStats();
    }

    private void loadStats() {
        File file = new File(STATS_FILE);

        if (!file.exists()) {
            System.out.println("No File creating one ....");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean skipHeader = true;

            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\|");
                if (parts.length >= 8) {
                    try {
                        String username = parts[0];
                        int highScore = Integer.parseInt(parts[1]);
                        int timesPlayed = Integer.parseInt(parts[2]);
                        long totalPlayTime = Long.parseLong(parts[3]);
                        String lastPlayDate = parts[4];
                        int gamesWon = Integer.parseInt(parts[5]);
                        String lastGameTime = parts[6];
                        String lastTimePlayed = parts.length > 7 ? parts[7] : "Never";

                        PlayerStats stats = new PlayerStats(username, highScore, timesPlayed,
                                totalPlayTime, lastPlayDate, gamesWon,
                                lastGameTime, lastTimePlayed);
                        playerStatsMap.put(username, stats);
                    } catch (NumberFormatException e) {
                        System.err.println(" Error " + line);
                    }
                }
            }
            System.out.println("Loaded  " + playerStatsMap.size() + " Players");
        } catch (IOException e) {
            System.err.println("Unable to read file: " + e.getMessage());
        }
    }

    /**
     * حفظ الإحصائيات إلى الملف
     */
    private void saveStats() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(STATS_FILE))) {
            writer.println("// Rocket Game - Player Statistics");
            writer.println("// Format: Username | HighScore | TimesPlayed | TotalPlayTime(s) | LastPlayDate | GamesWon | LastGameTime(MM:SS) | LastTimePlayed(YYYY-MM-DD HH:mm:ss)");
            writer.println("// Auto-generated file\n");

            for (PlayerStats stats : playerStatsMap.values()) {
                writer.println(stats.toString());
            }

            System.out.println("[STATS] تم حفظ بيانات " + playerStatsMap.size() + " لاعب بنجاح");
        } catch (IOException e) {
            System.err.println("[ERROR] خطأ في حفظ الإحصائيات: " + e.getMessage());
        }
    }

    /**
     * ✅ تنسيق وقت اللعبة (دقائق:ثواني)
     */
    private String formatGameTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    /**
     * تنسيق وقت اللعب المختصر للعرض
     */
    private String formatPlayTimeCompact(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    /**
     * ✅ تنسيق آخر وقت لعب (استخراج الساعة من التاريخ)
     */
    private String formatLastPlayTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.equals("Never")) {
            return "Never";
        }
        // الصيغة: "2025-12-10 01:22:45" -> يعيد "01:22"
        try {
            if (dateTimeStr.contains(" ")) {
                String[] parts = dateTimeStr.split(" ");
                if (parts.length >= 2) {
                    String time = parts[1]; // "01:22:45"
                    String[] timeParts = time.split(":");
                    return timeParts[0] + ":" + timeParts[1]; // "01:22"
                }
            }
            return dateTimeStr;
        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    /**
     * طباعة لوحة الترتيب
     */
    public void printLeaderboard() {
        if (playerStatsMap.isEmpty()) {
            System.out.println("\n========== LEADERBOARD ==========");
            System.out.println("لا توجد بيانات لاعبين بعد");
            System.out.println("===================================\n");
            return;
        }

        // ترتيب اللاعبين حسب الدرجة الأعلى
        List<PlayerStats> sortedStats = new ArrayList<>(playerStatsMap.values());
        sortedStats.sort((a, b) -> Integer.compare(b.highScore, a.highScore));

        System.out.println("\n========== LEADERBOARD ==========");
        System.out.printf("%-5s %-15s %-10s %-8s %-12s %-6s %-10s %-19s%n",
                "Rank", "Username", "HighScore", "Played", "TotalTime", "Wins", "LastGame", "LastTimePlayed");
        System.out.println("==============================================================================");

        int rank = 1;
        for (PlayerStats stats : sortedStats) {
            String totalPlayTime = formatPlayTimeCompact(stats.totalPlayTime);
            System.out.printf("%-5d %-15s %-10d %-8d %-12s %-6d %-10s %-19s%n",
                    rank++,
                    stats.username,
                    stats.highScore,
                    stats.timesPlayed,
                    totalPlayTime,
                    stats.gamesWon,
                    stats.lastGameTime,
                    stats.lastTimePlayed); // ✅ جديد: عرض آخر وقت لعب
        }
        System.out.println("==============================================================================\n");
    }

    /**
     * طباعة بيانات لاعب واحد فقط
     */
    public void printPlayerStats(String username) {
        PlayerStats stats = playerStatsMap.get(username);
        if (stats == null) {
            System.out.println("[ERROR] اللاعب '" + username + "' غير موجود");
            return;
        }

        System.out.println("\n========== PLAYER STATS ==========");
        System.out.println("Username: " + stats.username);
        System.out.println("High Score: " + stats.highScore);
        System.out.println("Times Played: " + stats.timesPlayed);
        System.out.println("Total Play Time: " + formatPlayTimeCompact(stats.totalPlayTime));
        System.out.println("Last Play Date: " + stats.lastPlayDate);
        System.out.println("Games Won: " + stats.gamesWon);
        System.out.println("Last Game Time: " + stats.lastGameTime);
        System.out.println("Last Played: " + stats.lastTimePlayed); // ✅ جديد
        System.out.println("===================================\n");
    }

    /**
     * الحصول على احصائيات لاعب معين
     */
    public PlayerStats getPlayerStats(String username) {
        return playerStatsMap.get(username);
    }

    /**
     * الحصول على جميع الإحصائيات
     */
    public Map<String, PlayerStats> getAllStats() {
        return new HashMap<>(playerStatsMap);
    }

    /**
     * عدد اللاعبين الكلي
     */
    public int getTotalPlayers() {
        return playerStatsMap.size();
    }

    /**
     * الحصول على متوسط وقت اللعب لكل لاعب
     */
    public long getAveragePlayTime(String username) {
        PlayerStats stats = getPlayerStats(username);
        if (stats != null && stats.timesPlayed > 0) {
            return stats.totalPlayTime / stats.timesPlayed;
        }
        return 0;
    }
}
