package RocketGame.Rendering;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import RocketGame.Core.GameEngine;
import RocketGame.Core.GameState;
import RocketGame.Entities.*;
import RocketGame.Effects.ParticleSystem;
import RocketGame.Audio.SoundManager;
import RocketGame.Util.Constants;
import RocketGame.Util.UserStatsManager;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Font;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class GameRenderer implements GLEventListener {
    private GameEngine gameEngine;
    private TextRenderer textRenderer;
    private UserStatsManager statsManager;
    private long gameStartTime;
    private boolean statsSaved = false;


    public static final int BUTTON_WIDTH = 200;
    public static final int BUTTON_HEIGHT = 50;
    public static final int RESTART_BTN_X = Constants.WINDOW_WIDTH / 2 - BUTTON_WIDTH / 2;
    public static final int RESTART_BTN_Y = 350;
    public static final int MENU_BTN_X = Constants.WINDOW_WIDTH / 2 - BUTTON_WIDTH / 2;
    public static final int MENU_BTN_Y = 420;

    private float[] starPositions;
    private float[] starSpeeds;
    private float[] starBrightness;
    private static final int STAR_COUNT = 100;

    public static final int PAUSE_BTN_X = Constants.WINDOW_WIDTH - 60;
    public static final int PAUSE_BTN_Y = 20;
    public static final int PAUSE_BTN_SIZE = 40;

    public static final int MUTE_BTN_X = Constants.WINDOW_WIDTH - 60;
    public static final int MUTE_BTN_Y = 70;
    public static final int MUTE_BTN_SIZE = 40;

    public static final int RESUME_BTN_X = Constants.WINDOW_WIDTH / 2 - 100;
    public static final int RESUME_BTN_Y = 250;
    public static final int EXIT_MENU_BTN_X = Constants.WINDOW_WIDTH / 2 - 100;
    public static final int EXIT_MENU_BTN_Y = 320;

    public GameRenderer(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        this.statsManager = new UserStatsManager();
        this.gameStartTime = System.currentTimeMillis();
        this.statsSaved = false;
        initializeStars();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, 0, -1, 1);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        gameEngine.update();

        GameState gameState = gameEngine.getGameState();
        ParticleSystem particleSystem = gameEngine.getParticleSystem();

        gameState.getRocket().render(gl);
        if (gameState.getRocket2() != null) {
            gameState.getRocket2().render(gl);
        }

        drawStarField(gl);

        gl.glDisable(GL.GL_TEXTURE_2D);
        particleSystem.render(gl);
        gl.glEnable(GL.GL_TEXTURE_2D);

        if (!gameState.isGameOver()) {
            for (int i = 0; i < gameState.getPowerups().size(); i++) {
                gameState.getPowerups().get(i).render(gl);
            }

            for (int i = 0; i < gameState.getObstacles().size(); i++) {
                gameState.getObstacles().get(i).render(gl);
            }

            for (int i = 0; i < gameState.getEnemies().size(); i++) {
                gameState.getEnemies().get(i).render(gl);
            }

            if (gameState.getBoss() != null) {
                gameState.getBoss().render(gl);
            }
        }

        for (int i = 0; i < gameState.getBullets().size(); i++) {
            gameState.getBullets().get(i).render(gl);
        }

        gameState.getRocket().render(gl);
        if (gameState.getRocket2() != null) {
            gameState.getRocket2().render(gl);
        }

        drawHUD(gl, gameState);

        if (gameState.isPaused()) {
            drawPauseScreen(gl);
        }

        if (gameState.isGameOver() && !statsSaved) {
            saveGameStats(gameState);
            statsSaved = true;
        }

        if (gameState.isGameOver()) {
            drawGameOverScreen(gl, gameState);
        }

        if (gameEngine.isShowingUpgradeMenu()) {
            drawUpgradeMenu(gl);
        }

        if (gameEngine.isLevelTransitioning()) {
            drawLevelTransition(gl, gameState);
        }

        gl.glFlush();
    }

    private int getPlayerRankByScore(String username, int currentScore) {
        int rank = 1;

        try {
            Map<String, UserStatsManager.PlayerStats> allStats = statsManager.getAllStats();

            if (allStats.isEmpty()) {
                System.out.println("No player found");
                return 1;
            }

            //Bkaren ben ella3eba
            for (UserStatsManager.PlayerStats playerStats : allStats.values()) {
                if (playerStats.highScore > currentScore) {
                    rank++;
                }
            }

            System.out.println("[RANK] Player: " + username +
                    ", Score: " + currentScore +
                    ", Rank: " + rank);

        } catch (Exception e) {
            System.out.println("[RANK] Error calculating rank: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }

        return rank;
    }

    private String getRankMedal(int rank) {
        switch (rank) {
            case 1: return "1ST PLACE";
            case 2: return "2ND PLACE";
            case 3: return "3RD PLACE";
            default: return "#" + rank;
        }
    }

    private float[] getRankColorByPosition(int rank) {
        switch (rank) {
            case 1: return new float[]{1.0f, 0.84f, 0.0f};
            case 2: return new float[]{0.75f, 0.75f, 0.75f};
            case 3: return new float[]{0.8f, 0.5f, 0.2f};
            default: return new float[]{1.0f, 1.0f, 1.0f};
        }
    }

    private void drawGameOverScreen(GL gl, GameState s) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.8f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        gl.glVertex2f(0, Constants.WINDOW_HEIGHT);
        gl.glEnd();

        String titleText;
        float[] c;

        if (s.isVictory()) {
            titleText = "VICTORY! MISSION ACCOMPLISHED";
            c = new float[]{0.0f, 1.0f, 0.0f};
        } else {
            titleText = "GAME OVER";
            c = new float[]{1.0f, 0.0f, 0.0f};
        }

        textRenderer.beginRendering(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        textRenderer.setColor(c[0], c[1], c[2], 1.0f);
        textRenderer.draw(titleText, Constants.WINDOW_WIDTH / 2 - 180, 400);
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        if (s.getRocket2() != null) {
            String p1ScoreText = "P1 SCORE: " + s.getScore();
            String p2ScoreText = "P2 SCORE: " + s.getScorePlayer2();
            textRenderer.draw(p1ScoreText, Constants.WINDOW_WIDTH / 2 - 100, 350);
            textRenderer.draw(p2ScoreText, Constants.WINDOW_WIDTH / 2 - 100, 310);
        } else {

            int finalScore = s.getScore();
            String username = s.getUsername();
            int playerRank = getPlayerRankByScore(username, finalScore);
            String rankMedal = getRankMedal(playerRank);
            float[] rankColor = getRankColorByPosition(playerRank);

            textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            textRenderer.draw("FINAL SCORE: " + finalScore, Constants.WINDOW_WIDTH / 2 - 100, 350);

            textRenderer.setColor(rankColor[0], rankColor[1], rankColor[2], 1.0f);
            textRenderer.draw("YOUR RANK: " + rankMedal, Constants.WINDOW_WIDTH / 2 - 100, 320);
        }

        textRenderer.endRendering();

        gl.glDisable(GL.GL_TEXTURE_2D);

        gl.glColor3f(0.2f, 0.8f, 0.2f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(RESTART_BTN_X, RESTART_BTN_Y);
        gl.glVertex2f(RESTART_BTN_X + BUTTON_WIDTH, RESTART_BTN_Y);
        gl.glVertex2f(RESTART_BTN_X + BUTTON_WIDTH, RESTART_BTN_Y + BUTTON_HEIGHT);
        gl.glVertex2f(RESTART_BTN_X, RESTART_BTN_Y + BUTTON_HEIGHT);
        gl.glEnd();
        drawSimpleText(gl, "PLAY AGAIN", RESTART_BTN_X + 40, RESTART_BTN_Y + 35);

        gl.glColor3f(0.8f, 0.2f, 0.2f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(MENU_BTN_X, MENU_BTN_Y);
        gl.glVertex2f(MENU_BTN_X + BUTTON_WIDTH, MENU_BTN_Y);
        gl.glVertex2f(MENU_BTN_X + BUTTON_WIDTH, MENU_BTN_Y + BUTTON_HEIGHT);
        gl.glVertex2f(MENU_BTN_X, MENU_BTN_Y + BUTTON_HEIGHT);
        gl.glEnd();
        drawSimpleText(gl, "EXIT GAME", MENU_BTN_X + 45, MENU_BTN_Y + 35);
    }

    private void drawSimpleText(GL gl, String text, float x, float y) {
        if (textRenderer == null) {
            textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
        }
        textRenderer.beginRendering(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        textRenderer.draw(text, (int) x, (int) (Constants.WINDOW_HEIGHT - y));
        textRenderer.endRendering();
    }

    private void drawHeart(GL gl, float x, float y) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1.0f, 0.2f, 0.2f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(x, y + 5);
        gl.glVertex2f(x + 5, y + 10);
        gl.glVertex2f(x + 10, y + 5);
        gl.glVertex2f(x, y + 5);
        gl.glVertex2f(x + 5, y);
        gl.glVertex2f(x + 10, y + 5);
        gl.glEnd();
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawHeart2(GL gl, float x, float y) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor3f(0.2f, 0.2f, 1.0f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(x, y + 5);
        gl.glVertex2f(x + 5, y + 10);
        gl.glVertex2f(x + 10, y + 5);
        gl.glVertex2f(x, y + 5);
        gl.glVertex2f(x + 5, y);
        gl.glVertex2f(x + 10, y + 5);
        gl.glEnd();
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawHealthBar(GL gl, Rocket rocket) {
        gl.glDisable(GL.GL_TEXTURE_2D);

        float barX = 10;
        float barY = 80;
        float w = 200;
        float h = 10;

        gl.glColor3f(0.5f, 0, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + w, barY);
        gl.glVertex2f(barX + w, barY + h);
        gl.glVertex2f(barX, barY + h);
        gl.glEnd();

        gl.glColor3f(1, 0, 0);
        float healthW = w * rocket.getHealthPercent();
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + healthW, barY);
        gl.glVertex2f(barX + healthW, barY + h);
        gl.glVertex2f(barX, barY + h);
        gl.glEnd();

        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawHealthBar(GL gl, Rocket rocket, float x, float y) {
        gl.glDisable(GL.GL_TEXTURE_2D);

        float barX = x;
        float barY = y;
        float w = 200;
        float h = 10;

        gl.glColor3f(0, 0, 0.5f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + w, barY);
        gl.glVertex2f(barX + w, barY + h);
        gl.glVertex2f(barX, barY + h);
        gl.glEnd();


        gl.glColor3f(0, 0, 1);
        float healthW = w * rocket.getHealthPercent();
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + healthW, barY);
        gl.glVertex2f(barX + healthW, barY + h);
        gl.glVertex2f(barX, barY + h);
        gl.glEnd();

        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawShieldBar(GL gl, Rocket rocket) {
        if (rocket.getShield() <= 0) return;

        gl.glDisable(GL.GL_TEXTURE_2D);

        float barX = 10;
        float barY = 100;
        float w = 200;
        float h = 10;

        gl.glColor3f(0, 0, 0.5f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + w, barY);
        gl.glVertex2f(barX + w, barY + h);
        gl.glVertex2f(barX, barY + h);
        gl.glEnd();

        gl.glColor3f(0, 0.5f, 1);
        float shieldW = w * rocket.getShieldPercent();
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + shieldW, barY);
        gl.glVertex2f(barX + shieldW, barY + h);
        gl.glVertex2f(barX, barY + h);
        gl.glEnd();

        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawPauseScreen(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        gl.glVertex2f(0, Constants.WINDOW_HEIGHT);
        gl.glEnd();

        gl.glColor3f(0.2f, 0.8f, 0.2f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(RESUME_BTN_X, RESUME_BTN_Y);
        gl.glVertex2f(RESUME_BTN_X + BUTTON_WIDTH, RESUME_BTN_Y);
        gl.glVertex2f(RESUME_BTN_X + BUTTON_WIDTH, RESUME_BTN_Y + BUTTON_HEIGHT);
        gl.glVertex2f(RESUME_BTN_X, RESUME_BTN_Y + BUTTON_HEIGHT);
        gl.glEnd();

        drawSimpleText(gl, "GAME PAUSED", Constants.WINDOW_WIDTH / 2 - 80, 150);
        drawSimpleText(gl, "RESUME", RESUME_BTN_X + 50, RESUME_BTN_Y + 35);
        drawSimpleText(gl, "EXIT GAME", MENU_BTN_X + 35, MENU_BTN_Y + 35);
    }

    private void drawUpgradeMenu(GL gl) {
        drawSimpleText(gl, "LEVEL UP! CHOOSE UPGRADE", 300, 200);
    }

    private void drawLevelTransition(GL gl, GameState s) {
        drawSimpleText(gl, "LEVEL " + (s.getLevel() + 1), 350, 300);
    }

    private void drawStarField(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glPointSize(2.0f);
        gl.glBegin(GL.GL_POINTS);

        for (int i = 0; i < STAR_COUNT; i++) {
            float x = starPositions[i * 2];
            float y = starPositions[i * 2 + 1];

            y += starSpeeds[i];
            if (y > Constants.WINDOW_HEIGHT) { y = 0; x = (float) (Math.random() * Constants.WINDOW_WIDTH); }
            starPositions[i * 2] = x; starPositions[i * 2 + 1] = y;
            gl.glColor3f(starBrightness[i], starBrightness[i], starBrightness[i]);
            gl.glVertex2f(x, y);

            gl.glColor3f(starBrightness[i], starBrightness[i], starBrightness[i]);
            gl.glVertex2f(x, y);
        }
        gl.glEnd();
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawHUD(GL gl, GameState gameState) {

        SoundManager soundManager = SoundManager.getInstance();
        boolean isMuted = soundManager.isMuted();

        drawSimpleText(gl, "SCORE: " + gameState.getScore(), 10, 30);
        textRenderer.draw("HIGH SCORE: " + gameState.getHighScore(), 280, Constants.WINDOW_HEIGHT - 80);
        drawSimpleText(gl, "LIVES: " + gameState.getLives(), 10, 60);
        drawSimpleText(gl, "LEVEL: " + gameState.getLevel(), Constants.WINDOW_WIDTH / 2 - 50, 30);
        drawSimpleText(gl, "P1: " + gameState.getUsername(), 10, 110);

        for (int i = 0; i < gameState.getLives(); i++) {
            drawHeart(gl, 150 + i * 25, 50);
        }

        drawHealthBar(gl, gameState.getRocket());
        drawShieldBar(gl, gameState.getRocket());

        if (gameState.getRocket2() != null) {
            float p2X = Constants.WINDOW_WIDTH - 220;
            drawSimpleText(gl, "SCORE: " + gameState.getScorePlayer2(), p2X, 30);
            drawSimpleText(gl, "LIVES: " + gameState.getLivesPlayer2(), p2X, 60);
            drawSimpleText(gl, "P2: " + gameState.getUsername2(), p2X, 110);
            drawHealthBar(gl, gameState.getRocket2(), p2X, 80);

            for (int i = 0; i < gameState.getLivesPlayer2(); i++) {
                drawHeart2(gl, p2X + 120 + i * 25, 50);
            }
        }

        if (!gameState.isGameOver()) {
            gl.glDisable(GL.GL_TEXTURE_2D);


            gl.glColor3f(0.3f, 0.3f, 0.3f);
            gl.glBegin(GL.GL_QUADS);
            gl.glVertex2f(PAUSE_BTN_X, PAUSE_BTN_Y);
            gl.glVertex2f(PAUSE_BTN_X + PAUSE_BTN_SIZE, PAUSE_BTN_Y);
            gl.glVertex2f(PAUSE_BTN_X + PAUSE_BTN_SIZE, PAUSE_BTN_Y + PAUSE_BTN_SIZE);
            gl.glVertex2f(PAUSE_BTN_X, PAUSE_BTN_Y + PAUSE_BTN_SIZE);
            gl.glEnd();

            gl.glColor3f(1f, 1f, 1f);
            gl.glLineWidth(3);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(PAUSE_BTN_X + 13, PAUSE_BTN_Y + 10);
            gl.glVertex2f(PAUSE_BTN_X + 13, PAUSE_BTN_Y + 30);
            gl.glVertex2f(PAUSE_BTN_X + 27, PAUSE_BTN_Y + 10);
            gl.glVertex2f(PAUSE_BTN_X + 27, PAUSE_BTN_Y + 30);
            gl.glEnd();

            gl.glColor3f(isMuted ? 1.0f : 0.3f, isMuted ? 0.5f : 0.3f, 0.3f);
            gl.glBegin(GL.GL_QUADS);
            gl.glVertex2f(MUTE_BTN_X, MUTE_BTN_Y);
            gl.glVertex2f(MUTE_BTN_X + MUTE_BTN_SIZE, MUTE_BTN_Y);
            gl.glVertex2f(MUTE_BTN_X + MUTE_BTN_SIZE, MUTE_BTN_Y + MUTE_BTN_SIZE);
            gl.glVertex2f(MUTE_BTN_X, MUTE_BTN_Y + MUTE_BTN_SIZE);
            gl.glEnd();

            gl.glColor3f(1f, 1f, 1f);

            if (isMuted) {
                gl.glLineWidth(2);
                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(MUTE_BTN_X + 10, MUTE_BTN_Y + 10);
                gl.glVertex2f(MUTE_BTN_X + 30, MUTE_BTN_Y + 30);
                gl.glVertex2f(MUTE_BTN_X + 30, MUTE_BTN_Y + 10);
                gl.glVertex2f(MUTE_BTN_X + 10, MUTE_BTN_Y + 30);
                gl.glEnd();
            } else {
                gl.glLineWidth(2);
                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(MUTE_BTN_X + 12, MUTE_BTN_Y + 10);
                gl.glVertex2f(MUTE_BTN_X + 12, MUTE_BTN_Y + 30);
                gl.glVertex2f(MUTE_BTN_X + 20, MUTE_BTN_Y + 15);
                gl.glVertex2f(MUTE_BTN_X + 20, MUTE_BTN_Y + 25);
                gl.glVertex2f(MUTE_BTN_X + 28, MUTE_BTN_Y + 18);
                gl.glVertex2f(MUTE_BTN_X + 28, MUTE_BTN_Y + 22);
                gl.glEnd();
            }

            gl.glEnable(GL.GL_TEXTURE_2D);
        }
    }

    private void initializeStars() {
        starPositions = new float[STAR_COUNT * 2];
        starSpeeds = new float[STAR_COUNT];
        starBrightness = new float[STAR_COUNT];

        for (int i = 0; i < STAR_COUNT; i++) {
            starPositions[i * 2] = (float) (Math.random() * Constants.WINDOW_WIDTH);
            starPositions[i * 2 + 1] = (float) (Math.random() * Constants.WINDOW_HEIGHT);
            starSpeeds[i] = 0.5f + (float) (Math.random() * 1.5f);
            starBrightness[i] = 0.5f + (float) (Math.random() * 0.5f);
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        if (height <= 0) {
            height = 1;
        }
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, 0, -1, 1);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    public boolean isMuteButtonClicked(int mouseX, int mouseY) {
        return mouseX >= MUTE_BTN_X && mouseX <= MUTE_BTN_X + MUTE_BTN_SIZE &&
                mouseY >= MUTE_BTN_Y && mouseY <= MUTE_BTN_Y + MUTE_BTN_SIZE;
    }

    public void toggleMute() {
        SoundManager soundManager = SoundManager.getInstance();
        soundManager.toggleMute();
        System.out.println("Mute: " + soundManager.isMuted());
    }

    public boolean isMuted() {
        return SoundManager.getInstance().isMuted();
    }

    private void saveGameStats(GameState gameState) {
        long endTime = System.currentTimeMillis();
        long playTimeMs = endTime - gameStartTime;
        long playTimeSeconds = playTimeMs / 1000;

        String username = gameState.getUsername();
        int finalScore = gameState.getScore();
        boolean isVictory = gameState.isVictory();

        statsManager.updatePlayerStats(username, finalScore, playTimeSeconds, isVictory);

        System.out.println("GAME END: " + gameEngine.getGameTimeCalculator().getFormattedGameTime());
        System.out.println("GAME END - Player: " + username);
        System.out.println("GAME END - Final Score: " + finalScore);
        System.out.println("GAME END - Play Time: " + formatPlayTime(playTimeSeconds));
        System.out.println("GAME END - Victory: " + isVictory);
        System.out.println("=== Leaderboard ===");
        statsManager.printLeaderboard();
    }

    private String formatPlayTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%02d:%02d", minutes, secs);
        } else {
            return String.format("%d", secs);
        }
    }
}