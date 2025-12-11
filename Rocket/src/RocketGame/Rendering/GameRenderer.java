package RocketGame.Rendering;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import RocketGame.Core.GameEngine;
import RocketGame.Core.GameState;
import RocketGame.Entities.*;
import RocketGame.Effects.ParticleSystem;
import RocketGame.Util.Constants;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Font;

public class GameRenderer implements GLEventListener {
    private GameEngine gameEngine;
    private TextRenderer textRenderer;
    private TextRenderer upgradeTextRenderer;
    private boolean initialized = false;

    // ثوابت الأزرار
    public static final int BUTTON_WIDTH = 200;
    public static final int BUTTON_HEIGHT = 50;
    public static final int BUTTON_SPACING = 70;

    // إحداثيات الأزرار الثلاثة للمستوى الثالث
    public static final int PLAY_AGAIN_BTN_X = Constants.WINDOW_WIDTH / 2 - BUTTON_WIDTH / 2;
    public static final int PLAY_AGAIN_BTN_Y = 280;

    public static final int HOME_BTN_X = Constants.WINDOW_WIDTH / 2 - BUTTON_WIDTH / 2;
    public static final int HOME_BTN_Y = 350;

    public static final int EXIT_BTN_X = Constants.WINDOW_WIDTH / 2 - BUTTON_WIDTH / 2;
    public static final int EXIT_BTN_Y = 420;

    // الأزرار القديمة (للسابق مع Game Over العادي)
    public static final int RESTART_BTN_X = Constants.WINDOW_WIDTH / 2 - BUTTON_WIDTH / 2;
    public static final int RESTART_BTN_Y = 280 + 70; // تم تعديله في drawTwoButtons

    public static final int MENU_BTN_X = Constants.WINDOW_WIDTH / 2 - BUTTON_WIDTH / 2;
    public static final int MENU_BTN_Y = 420;

    private float[] starPositions;
    private float[] starSpeeds;
    private float[] starBrightness;
    private static final int STAR_COUNT = 100;

    public GameRenderer(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        initializeStars();
        // تهيئة TextRenderer في الكونستركتور
        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
        upgradeTextRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 20));
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
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // تأكد من تهيئة TextRenderer إذا لم يتم ذلك
        if (textRenderer == null) {
            textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
        }
        if (upgradeTextRenderer == null) {
            upgradeTextRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 20));
        }

        initialized = true;
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        // تأكد من تهيئة OpenGL إذا لم يتم ذلك
        if (!initialized) {
            init(drawable);
        }

        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        // تأكد من وجود gameEngine
        if (gameEngine == null) {
            System.err.println("GameEngine is null!");
            return;
        }

        gameEngine.update();

        GameState gameState = gameEngine.getGameState();
        ParticleSystem particleSystem = gameEngine.getParticleSystem();

        drawStarField(gl);
        particleSystem.render(gl);

        if (!gameState.isGameOver()) {
            for (int i = 0; i < gameState.getPowerups().size(); i++) gameState.getPowerups().get(i).render(gl);
            for (int i = 0; i < gameState.getObstacles().size(); i++) gameState.getObstacles().get(i).render(gl);
            for (int i = 0; i < gameState.getEnemies().size(); i++) gameState.getEnemies().get(i).render(gl);
            if (gameState.getBoss() != null) gameState.getBoss().render(gl);
            for (int i = 0; i < gameState.getBullets().size(); i++) gameState.getBullets().get(i).render(gl);

            gameState.getRocket().render(gl);
            if (gameState.getRocket2() != null) {
                gameState.getRocket2().render(gl);
            }
        }

        drawHUD(gl, gameState);

        if (gameState.isPaused()) {
            drawPauseScreen(gl);
        }

        // التعديل: التحقق إذا كانت شاشة انتهاء المستوى الثالث أم لا
        if (gameState.isGameOver() && gameEngine.isLevelThreeComplete()) {
            drawLevelThreeCompleteScreen(gl);
        } else if (gameState.isGameOver()) {
            drawGameOverScreen(gl, gameState);
        }

        // قائمة الترقية تظهر فقط من المستوى 2 فما فوق
        if (gameEngine.isShowingUpgradeMenu()) {
            drawUpgradeMenu(gl);
        }

        // عرض رسالة انتقال المستوى
        if (gameEngine.isLevelTransitioning()) {
            drawLevelTransition(gl, gameState);
        }

        gl.glFlush();
    }

    private void drawLevelThreeCompleteScreen(GL gl) {
        // خلفية شفافة فقط
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.8f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0); gl.glVertex2f(Constants.WINDOW_WIDTH, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT); gl.glVertex2f(0, Constants.WINDOW_HEIGHT);
        gl.glEnd();
        gl.glEnable(GL.GL_TEXTURE_2D);

        // عرض الأزرار الثلاثة فقط بدون أي نصوص
        drawThreeButtons(gl);
    }

    private void drawThreeButtons(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);

        // 1. زر Play Again (أخضر)
        gl.glColor3f(0.2f, 0.8f, 0.2f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(PLAY_AGAIN_BTN_X, PLAY_AGAIN_BTN_Y);
        gl.glVertex2f(PLAY_AGAIN_BTN_X + BUTTON_WIDTH, PLAY_AGAIN_BTN_Y);
        gl.glVertex2f(PLAY_AGAIN_BTN_X + BUTTON_WIDTH, PLAY_AGAIN_BTN_Y + BUTTON_HEIGHT);
        gl.glVertex2f(PLAY_AGAIN_BTN_X, PLAY_AGAIN_BTN_Y + BUTTON_HEIGHT);
        gl.glEnd();
        drawSimpleText(gl, "PLAY AGAIN", PLAY_AGAIN_BTN_X + 40, PLAY_AGAIN_BTN_Y + 35);

        // 2. زر Back to Home (أزرق)
        gl.glColor3f(0.2f, 0.2f, 0.8f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(HOME_BTN_X, HOME_BTN_Y);
        gl.glVertex2f(HOME_BTN_X + BUTTON_WIDTH, HOME_BTN_Y);
        gl.glVertex2f(HOME_BTN_X + BUTTON_WIDTH, HOME_BTN_Y + BUTTON_HEIGHT);
        gl.glVertex2f(HOME_BTN_X, HOME_BTN_Y + BUTTON_HEIGHT);
        gl.glEnd();
        drawSimpleText(gl, "BACK TO HOME", HOME_BTN_X + 30, HOME_BTN_Y + 35);

        // 3. زر Exit Game (أحمر)
        gl.glColor3f(0.8f, 0.2f, 0.2f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(EXIT_BTN_X, EXIT_BTN_Y);
        gl.glVertex2f(EXIT_BTN_X + BUTTON_WIDTH, EXIT_BTN_Y);
        gl.glVertex2f(EXIT_BTN_X + BUTTON_WIDTH, EXIT_BTN_Y + BUTTON_HEIGHT);
        gl.glVertex2f(EXIT_BTN_X, EXIT_BTN_Y + BUTTON_HEIGHT);
        gl.glEnd();
        drawSimpleText(gl, "EXIT GAME", EXIT_BTN_X + 45, EXIT_BTN_Y + 35);

        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawGameOverScreen(GL gl, GameState s) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.8f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0); gl.glVertex2f(Constants.WINDOW_WIDTH, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT); gl.glVertex2f(0, Constants.WINDOW_HEIGHT);
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

        // التحقق من textRenderer
        if (textRenderer == null) {
            textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
        }

        textRenderer.beginRendering(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        textRenderer.setColor(c[0], c[1], c[2], 1.0f);
        textRenderer.draw(titleText, Constants.WINDOW_WIDTH / 2 - 180, 400);
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        textRenderer.draw("FINAL SCORE - Player 1: " + s.getScore(), Constants.WINDOW_WIDTH / 2 - 120, 350);
        if (s.isTwoPlayerMode()) {
            textRenderer.draw("FINAL SCORE - Player 2: " + s.getScorePlayer2(), Constants.WINDOW_WIDTH / 2 - 120, 320);
        }
        textRenderer.endRendering();

        // عرض زرين فقط في حالة Game Over العادية
        drawTwoButtons(gl);
    }

    private void drawTwoButtons(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);

        // زر Play Again (أخضر)
        gl.glColor3f(0.2f, 0.8f, 0.2f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(RESTART_BTN_X, RESTART_BTN_Y);
        gl.glVertex2f(RESTART_BTN_X + BUTTON_WIDTH, RESTART_BTN_Y);
        gl.glVertex2f(RESTART_BTN_X + BUTTON_WIDTH, RESTART_BTN_Y + BUTTON_HEIGHT);
        gl.glVertex2f(RESTART_BTN_X, RESTART_BTN_Y + BUTTON_HEIGHT);
        gl.glEnd();
        drawSimpleText(gl, "PLAY AGAIN", RESTART_BTN_X + 40, RESTART_BTN_Y + 35);

        // زر Exit Game (أحمر)
        gl.glColor3f(0.8f, 0.2f, 0.2f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(MENU_BTN_X, MENU_BTN_Y);
        gl.glVertex2f(MENU_BTN_X + BUTTON_WIDTH, MENU_BTN_Y);
        gl.glVertex2f(MENU_BTN_X + BUTTON_WIDTH, MENU_BTN_Y + BUTTON_HEIGHT);
        gl.glVertex2f(MENU_BTN_X, MENU_BTN_Y + BUTTON_HEIGHT);
        gl.glEnd();
        drawSimpleText(gl, "EXIT GAME", MENU_BTN_X + 45, MENU_BTN_Y + 35);

        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawSimpleText(GL gl, String text, float x, float y) {
        // التحقق من textRenderer في كل مرة
        if (textRenderer == null) {
            textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
        }

        textRenderer.beginRendering(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        textRenderer.draw(text, (int)x, (int)(Constants.WINDOW_HEIGHT - y));
        textRenderer.endRendering();
    }

    private void drawHeart(GL gl, float x, float y) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1.0f, 0.2f, 0.2f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(x, y + 5); gl.glVertex2f(x + 5, y + 10); gl.glVertex2f(x + 10, y + 5);
        gl.glVertex2f(x, y + 5); gl.glVertex2f(x + 5, y); gl.glVertex2f(x + 10, y + 5);
        gl.glEnd();
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawBlueHeart(GL gl, float x, float y) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor3f(0.2f, 0.2f, 1.0f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(x, y + 5); gl.glVertex2f(x + 5, y + 10); gl.glVertex2f(x + 10, y + 5);
        gl.glVertex2f(x, y + 5); gl.glVertex2f(x + 5, y); gl.glVertex2f(x + 10, y + 5);
        gl.glEnd();
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawHealthBar(GL gl, Rocket rocket, float x, float y, boolean isPlayer2) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        float w = 150; float h = 8;
        gl.glColor3f(0.5f, 0, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y); gl.glVertex2f(x+w, y);
        gl.glVertex2f(x+w, y+h); gl.glVertex2f(x, y+h);
        gl.glEnd();

        if (isPlayer2) {
            gl.glColor3f(0, 0, 1);
        } else {
            gl.glColor3f(1, 0, 0);
        }

        float healthW = w * rocket.getHealthPercent();
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y); gl.glVertex2f(x+healthW, y);
        gl.glVertex2f(x+healthW, y+h); gl.glVertex2f(x, y+h);
        gl.glEnd();
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawShieldBar(GL gl, Rocket rocket, float x, float y) {
        if(rocket.getShield() <= 0) return;
        gl.glDisable(GL.GL_TEXTURE_2D);
        float w = 150; float h = 8;
        gl.glColor3f(0, 0, 0.5f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y); gl.glVertex2f(x+w, y);
        gl.glVertex2f(x+w, y+h); gl.glVertex2f(x, y+h);
        gl.glEnd();
        gl.glColor3f(0, 0.5f, 1);
        float shieldW = w * rocket.getShieldPercent();
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y); gl.glVertex2f(x+shieldW, y);
        gl.glVertex2f(x+shieldW, y+h); gl.glVertex2f(x, y+h);
        gl.glEnd();
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawPauseScreen(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0); gl.glVertex2f(Constants.WINDOW_WIDTH, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT); gl.glVertex2f(0, Constants.WINDOW_HEIGHT);
        gl.glEnd();

        drawSimpleText(gl, "PAUSED", Constants.WINDOW_WIDTH/2 - 50, Constants.WINDOW_HEIGHT/2);
        drawSimpleText(gl, "Press ESC to resume", Constants.WINDOW_WIDTH/2 - 100, Constants.WINDOW_HEIGHT/2 - 40);
    }

    private void drawUpgradeMenu(GL gl) {
        // خلفية شفافة
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.8f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0); gl.glVertex2f(Constants.WINDOW_WIDTH, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT); gl.glVertex2f(0, Constants.WINDOW_HEIGHT);
        gl.glEnd();

        // تأكد من تهيئة textRenderers
        if (textRenderer == null) {
            textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
        }
        if (upgradeTextRenderer == null) {
            upgradeTextRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 20));
        }

        // عنوان الترقية مع إظهار المستوى الحالي
        textRenderer.beginRendering(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        textRenderer.setColor(0.0f, 1.0f, 0.0f, 1.0f);
        textRenderer.draw("LEVEL UP!", Constants.WINDOW_WIDTH/2 - 60, 400);
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        textRenderer.draw("LEVEL " + (gameEngine.getGameState().getLevel()) + " COMPLETE!", Constants.WINDOW_WIDTH/2 - 120, 370);
        textRenderer.draw("CHOOSE UPGRADE", Constants.WINDOW_WIDTH/2 - 100, 340);
        textRenderer.endRendering();

        // خيارات الترقية
        String[] options = gameEngine.getUpgradeOptions();
        int selected = gameEngine.getSelectedUpgrade();

        upgradeTextRenderer.beginRendering(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        for (int i = 0; i < options.length; i++) {
            if (i == selected) {
                upgradeTextRenderer.setColor(1.0f, 1.0f, 0.0f, 1.0f);
                upgradeTextRenderer.draw("> " + options[i], Constants.WINDOW_WIDTH/2 - 80, 280 - i*40);
            } else {
                upgradeTextRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                upgradeTextRenderer.draw("  " + options[i], Constants.WINDOW_WIDTH/2 - 80, 280 - i*40);
            }
        }
        upgradeTextRenderer.setColor(0.8f, 0.8f, 0.8f, 1.0f);
        upgradeTextRenderer.draw("Press SPACE or ENTER to select", Constants.WINDOW_WIDTH/2 - 140, 150);
        upgradeTextRenderer.endRendering();
    }

    private void drawLevelTransition(GL gl, GameState s) {
        // خلفية شفافة
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.6f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0); gl.glVertex2f(Constants.WINDOW_WIDTH, 0);
        gl.glVertex2f(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT); gl.glVertex2f(0, Constants.WINDOW_HEIGHT);
        gl.glEnd();
        gl.glEnable(GL.GL_TEXTURE_2D);

        if (textRenderer == null) {
            textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
        }

        textRenderer.beginRendering(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        // رسالة خاصة للمستوى الأول
        if (s.getLevel() == 1) {
            textRenderer.setColor(0.0f, 1.0f, 1.0f, 1.0f);
            textRenderer.draw("LEVEL 1 COMPLETE!", Constants.WINDOW_WIDTH/2 - 120, Constants.WINDOW_HEIGHT/2 + 40);
            textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            textRenderer.draw("GET READY FOR LEVEL 2!", Constants.WINDOW_WIDTH/2 - 140, Constants.WINDOW_HEIGHT/2);
        }
        // رسالة عامة للمستويات الأخرى
        else {
            textRenderer.setColor(0.0f, 1.0f, 1.0f, 1.0f);
            textRenderer.draw("LEVEL " + s.getLevel() + " COMPLETE!",
                    Constants.WINDOW_WIDTH/2 - 120, Constants.WINDOW_HEIGHT/2 + 40);
            textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            textRenderer.draw("PREPARING LEVEL " + (s.getLevel() + 1),
                    Constants.WINDOW_WIDTH/2 - 130, Constants.WINDOW_HEIGHT/2);
        }

        textRenderer.endRendering();
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
        }
        gl.glEnd();
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawHUD(GL gl, GameState gameState) {
        // التحقق من textRenderer
        if (textRenderer == null) {
            textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
        }

        // Player 1 info
        drawSimpleText(gl, "P1 SCORE: " + gameState.getScore(), 10, 30);
        drawSimpleText(gl, "P1 LIVES: " + gameState.getLives(), 10, 60);

        for (int i = 0; i < gameState.getLives(); i++) {
            drawHeart(gl, 120 + i * 25, 50);
        }

        drawHealthBar(gl, gameState.getRocket(), 10, 90, false);
        drawShieldBar(gl, gameState.getRocket(), 10, 105);

        // Player 2 info (if exists)
        if (gameState.isTwoPlayerMode() && gameState.getRocket2() != null) {
            drawSimpleText(gl, "P2 SCORE: " + gameState.getScorePlayer2(), Constants.WINDOW_WIDTH - 200, 30);
            drawSimpleText(gl, "P2 LIVES: " + gameState.getLivesPlayer2(), Constants.WINDOW_WIDTH - 200, 60);

            for (int i = 0; i < gameState.getLivesPlayer2(); i++) {
                drawBlueHeart(gl, Constants.WINDOW_WIDTH - 80 + i * 25, 50);
            }

            drawHealthBar(gl, gameState.getRocket2(), Constants.WINDOW_WIDTH - 200, 90, true);
            drawShieldBar(gl, gameState.getRocket2(), Constants.WINDOW_WIDTH - 200, 105);
        }

        // Level info
        drawSimpleText(gl, "LEVEL: " + gameState.getLevel(), Constants.WINDOW_WIDTH / 2 - 50, 30);

        // Combo info
        if (gameState.getCombo() > 1) {
            drawSimpleText(gl, "COMBO x" + gameState.getCombo(), Constants.WINDOW_WIDTH / 2 - 50, 60);
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
        if (height <= 0) height = 1;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, 0, -1, 1);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
}