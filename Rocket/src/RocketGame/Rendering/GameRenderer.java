package RocketGame.Rendering;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import RocketGame.Core.GameEngine;
import RocketGame.Core.GameState;
import RocketGame.Entities.*;
import RocketGame.Effects.ParticleSystem;
import RocketGame.Util.Constants;

public class GameRenderer implements GLEventListener {
    private GameEngine gameEngine;

    private float[] starPositions;
    private float[] starSpeeds;
    private float[] starBrightness;
    private static final int STAR_COUNT = 100;

    public GameRenderer(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        initializeStars();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        gl.glOrtho(0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, 0, -1, 1);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        gameEngine.update();

        GameState gameState = gameEngine.getGameState();
        ParticleSystem particleSystem = gameEngine.getParticleSystem();

        drawStarField(gl);

        particleSystem.render(gl);

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

            for (int i = 0; i < gameState.getBullets().size(); i++) {
                gameState.getBullets().get(i).render(gl);
            }

            gameState.getRocket().render(gl);
        }

        drawHUD(gl, gameState);

        if (gameState.isPaused()) {
            drawPauseScreen(gl);
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

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();

        if (height <= 0) {
            height = 1;
        }

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        // Manual orthographic projection
        gl.glOrtho(0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, 0, -1, 1);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    private void drawStarField(GL gl) {
        gl.glPointSize(2.0f);
        gl.glBegin(GL.GL_POINTS);

        for (int i = 0; i < STAR_COUNT; i++) {
            // Get position
            float x = starPositions[i * 2];
            float y = starPositions[i * 2 + 1];

            // Update position (move down)
            y += starSpeeds[i];

            // Wrap around when star goes off bottom
            if (y > Constants.WINDOW_HEIGHT) {
                y = 0;
                x = (float) (Math.random() * Constants.WINDOW_WIDTH);
            }

            // Save new position
            starPositions[i * 2] = x;
            starPositions[i * 2 + 1] = y;

            // Draw star with brightness
            gl.glColor3f(starBrightness[i], starBrightness[i], starBrightness[i]);
            gl.glVertex2f(x, y);
        }

        gl.glEnd();
    }

    private void drawHUD(GL gl, GameState gameState) {
        // Score (top left)
        drawSimpleText(gl, "SCORE: " + gameState.getScore(), 10, 30);

        // Level (top center)
        String levelText = "LEVEL: " + gameState.getLevel();
        drawSimpleText(gl, levelText, Constants.WINDOW_WIDTH / 2 - 60, 30);

        // High Score (top right)
        drawSimpleText(gl, "HIGH: " + gameState.getHighScore(), Constants.WINDOW_WIDTH - 180, 30);

        // Lives (below score)
        drawSimpleText(gl, "LIVES: " + gameState.getLives(), 10, 60);

        // Draw hearts for lives
        for (int i = 0; i < gameState.getLives(); i++) {
            drawHeart(gl, 120 + i * 25, 50);
        }

        // Health bar
        drawHealthBar(gl, gameState.getRocket());

        // Shield bar
        drawShieldBar(gl, gameState.getRocket());

        // Combo
        if (gameState.getCombo() > 1) {
            String comboText = gameState.getCombo() + "X COMBO!";
            float comboX = Constants.WINDOW_WIDTH / 2 - 80;
            gl.glColor3f(1.0f, 1.0f, 0.0f); // Yellow
            drawSimpleText(gl, comboText, comboX, 120);
        }
    }

    private void drawHeart(GL gl, float x, float y) {
        gl.glColor3f(1.0f, 0.2f, 0.2f); // Red

        // Simple heart using triangles
        gl.glBegin(GL.GL_TRIANGLES);
        // Bottom point
        gl.glVertex2f(x + 7, y + 12);
        gl.glVertex2f(x, y + 4);
        gl.glVertex2f(x + 4, y);

        gl.glVertex2f(x + 7, y + 12);
        gl.glVertex2f(x + 14, y + 4);
        gl.glVertex2f(x + 10, y);

        // Top bumps
        gl.glVertex2f(x + 4, y);
        gl.glVertex2f(x + 7, y + 2);
        gl.glVertex2f(x + 7, y + 4);

        gl.glVertex2f(x + 10, y);
        gl.glVertex2f(x + 7, y + 2);
        gl.glVertex2f(x + 7, y + 4);
        gl.glEnd();
    }

    private void drawHealthBar(GL gl, Rocket rocket) {
        float barX = 10;
        float barY = 90;
        float barWidth = 200;
        float barHeight = 15;

        // Label
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        drawSmallText(gl, "HEALTH", barX, barY - 5);

        // Background (dark red)
        gl.glColor3f(0.3f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // Health (red)
        float healthWidth = barWidth * rocket.getHealthPercent();
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + healthWidth, barY);
        gl.glVertex2f(barX + healthWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // Border
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();
    }

    private void drawShieldBar(GL gl, Rocket rocket) {
        if (rocket.getShield() <= 0) {
            return; // Don't draw if no shield
        }

        float barX = 10;
        float barY = 120;
        float barWidth = 200;
        float barHeight = 15;

        // Label
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        drawSmallText(gl, "SHIELD", barX, barY - 5);

        // Background (dark blue)
        gl.glColor3f(0.0f, 0.0f, 0.3f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // Shield (blue)
        float shieldWidth = barWidth * rocket.getShieldPercent();
        gl.glColor3f(0.3f, 0.7f, 1.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + shieldWidth, barY);
        gl.glVertex2f(barX + shieldWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // Border
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();
    }

    private void drawPauseScreen(GL gl) {
        // Semi-transparent dark overlay
        gl.glColor3f(0.0f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        gl.glVertex2f(0, Constants.WINDOW_HEIGHT);
        gl.glEnd();

        // "PAUSED" text
        float centerX = Constants.WINDOW_WIDTH / 2 - 100;
        float centerY = Constants.WINDOW_HEIGHT / 2;

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        drawBigText(gl, "PAUSED", centerX, centerY);

        // Instructions
        drawSimpleText(gl, "PRESS ESC TO RESUME", centerX - 30, centerY + 60);
    }

    private void drawGameOverScreen(GL gl, GameState gameState) {
        // Dark overlay
        gl.glColor3f(0.0f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        gl.glVertex2f(0, Constants.WINDOW_HEIGHT);
        gl.glEnd();

        // "GAME OVER" text
        float centerX = Constants.WINDOW_WIDTH / 2;
        float centerY = Constants.WINDOW_HEIGHT / 2;

        gl.glColor3f(1.0f, 0.0f, 0.0f);
        drawBigText(gl, "GAME OVER", centerX - 150, centerY - 50);

        // Final score
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        String scoreText = "FINAL SCORE: " + gameState.getScore();
        drawSimpleText(gl, scoreText, centerX - 120, centerY + 20);

        // High score
        if (gameState.getScore() >= gameState.getHighScore()) {
            gl.glColor3f(1.0f, 1.0f, 0.0f);
            drawSimpleText(gl, "NEW HIGH SCORE!", centerX - 120, centerY + 50);
        } else {
            gl.glColor3f(1.0f, 1.0f, 1.0f);
            String highText = "HIGH SCORE: " + gameState.getHighScore();
            drawSimpleText(gl, highText, centerX - 110, centerY + 50);
        }

        // Instructions
        drawSimpleText(gl, "PRESS SPACE TO RESTART", centerX - 140, centerY + 100);
    }

    private void drawUpgradeMenu(GL gl) {
        // Dark overlay
        gl.glColor3f(0.0f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        gl.glVertex2f(0, Constants.WINDOW_HEIGHT);
        gl.glEnd();

        // Title
        float centerX = Constants.WINDOW_WIDTH / 2;
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        drawBigText(gl, "LEVEL UP!", centerX - 120, 150);

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        drawSimpleText(gl, "CHOOSE AN UPGRADE:", centerX - 140, 210);

        // Upgrade options
        String[] options = gameEngine.getUpgradeOptions();
        int selected = gameEngine.getSelectedUpgrade();

        for (int i = 0; i < options.length; i++) {
            float y = 280 + i * 60;

            // Highlight selected option
            if (i == selected) {
                // Draw selection box
                gl.glColor3f(0.3f, 0.3f, 0.8f);
                gl.glBegin(GL.GL_QUADS);
                gl.glVertex2f(centerX - 180, y - 15);
                gl.glVertex2f(centerX + 180, y - 15);
                gl.glVertex2f(centerX + 180, y + 35);
                gl.glVertex2f(centerX - 180, y + 35);
                gl.glEnd();

                // Arrow
                gl.glColor3f(1.0f, 1.0f, 0.0f);
                drawSimpleText(gl, ">", centerX - 160, y + 5);
            }

            // Option text
            gl.glColor3f(1.0f, 1.0f, 1.0f);
            drawSimpleText(gl, options[i], centerX - 130, y + 5);
        }

        // Instructions
        gl.glColor3f(0.7f, 0.7f, 0.7f);
        drawSmallText(gl, "USE ARROW KEYS TO SELECT", centerX - 140, 520);
        drawSmallText(gl, "PRESS SPACE TO CONFIRM", centerX - 130, 545);
    }

    private void drawLevelTransition(GL gl, GameState gameState) {
        float centerX = Constants.WINDOW_WIDTH / 2;
        float centerY = Constants.WINDOW_HEIGHT / 2;

        gl.glColor3f(1.0f, 1.0f, 0.0f);
        String levelText = "LEVEL " + gameState.getLevel();
        drawBigText(gl, levelText, centerX - 100, centerY);
    }

    private void drawBigText(GL gl, String text, float x, float y) {
        float charWidth = 30;
        float charHeight = 40;

        for (int i = 0; i < text.length(); i++) {
            float charX = x + i * charWidth;

            gl.glBegin(GL.GL_QUADS);
            gl.glVertex2f(charX, y);
            gl.glVertex2f(charX + charWidth - 5, y);
            gl.glVertex2f(charX + charWidth - 5, y + charHeight);
            gl.glVertex2f(charX, y + charHeight);
            gl.glEnd();
        }
    }

    private void drawSimpleText(GL gl, String text, float x, float y) {
        float charWidth = 15;
        float charHeight = 20;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            float charX = x + i * charWidth;

            // Skip spaces
            if (c == ' ') continue;

            // Draw filled rectangle for each character
            gl.glBegin(GL.GL_QUADS);
            gl.glVertex2f(charX, y);
            gl.glVertex2f(charX + charWidth - 3, y);
            gl.glVertex2f(charX + charWidth - 3, y + charHeight);
            gl.glVertex2f(charX, y + charHeight);
            gl.glEnd();
        }
    }

    private void drawSmallText(GL gl, String text, float x, float y) {
        float charWidth = 10;
        float charHeight = 12;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            float charX = x + i * charWidth;

            if (c == ' ') continue;

            gl.glBegin(GL.GL_QUADS);
            gl.glVertex2f(charX, y);
            gl.glVertex2f(charX + charWidth - 2, y);
            gl.glVertex2f(charX + charWidth - 2, y + charHeight);
            gl.glVertex2f(charX, y + charHeight);
            gl.glEnd();
        }
    }
    private void initializeStars() {
        starPositions = new float[STAR_COUNT * 2]; // x, y pairs
        starSpeeds = new float[STAR_COUNT];
        starBrightness = new float[STAR_COUNT];

        for (int i = 0; i < STAR_COUNT; i++) {
            // Random positions
            starPositions[i * 2] = (float) (Math.random() * Constants.WINDOW_WIDTH);
            starPositions[i * 2 + 1] = (float) (Math.random() * Constants.WINDOW_HEIGHT);

            // Random speeds (slow)
            starSpeeds[i] = 0.5f + (float) (Math.random() * 1.5f);

            // Random brightness
            starBrightness[i] = 0.5f + (float) (Math.random() * 0.5f);
        }
    }
}