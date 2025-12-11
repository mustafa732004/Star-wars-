package RocketGame.Util;

public class GameTimeCalculator {

    private long gameStartTime;
    private long totalPauseTime;
    private long pauseStartTime;
    private boolean isPaused;

    public GameTimeCalculator() {
        reset();
    }

    public void reset() {
        this.gameStartTime = System.currentTimeMillis();
        this.totalPauseTime = 0;
        this.pauseStartTime = 0;
        this.isPaused = false;
    }

    public void startPause() {
        if (!isPaused) {
            this.isPaused = true;
            this.pauseStartTime = System.currentTimeMillis();
            System.out.println("GameTimeCalculator   Pause started at: " + pauseStartTime);
        }
    }

    public void endPause() {
        if (isPaused) {
            long pauseDuration = System.currentTimeMillis() - pauseStartTime;
            this.totalPauseTime += pauseDuration;
            this.isPaused = false;
            System.out.println("GameTimeCalculator  Pause ended. Duration: " + pauseDuration + "ms");
            System.out.println("GameTimeCalculator Total pause time: " + totalPauseTime + "ms");
        }
    }

public long getGameTimeInSeconds() {
        long currentPauseTime = 0;

        if (isPaused) {
            currentPauseTime = System.currentTimeMillis() - pauseStartTime;
        }

        long gameTime = (System.currentTimeMillis() - gameStartTime - totalPauseTime - currentPauseTime) / 1000;

        return gameTime;
    }

    public long getGameTimeInMinutes() {
        return getGameTimeInSeconds() / 60;
    }

    public String getFormattedGameTime() {
        long totalSeconds = getGameTimeInSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getShortFormattedTime() {
        long totalSeconds = getGameTimeInSeconds();
        if (totalSeconds < 60) {
            return totalSeconds + "s";
        } else {
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            return minutes + "m " + seconds + "s";
        }
    }

    public String getMinutesAndSeconds() {
        return getFormattedGameTime();
    }

    public long getTotalElapsedTime() {
        return (System.currentTimeMillis() - gameStartTime) / 1000;
    }

    public long getTotalPauseTime() {
        return totalPauseTime / 1000;
    }

    public boolean isPaused() {
        return isPaused;
    }
}