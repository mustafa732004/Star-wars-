package RocketGame.Input;

import RocketGame.Core.GameEngine;
import RocketGame.Main.RocketGame;
import RocketGame.Rendering.GameRenderer;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {

    private final Set<Integer> pressedKeys = new HashSet<>();
    private int mouseX, mouseY;
    private boolean mousePressed = false;

    private GameEngine gameEngine;
    public RocketGame mainGame; // جعلناها public للوصول من GameEngine

    public InputHandler(GameEngine engine, RocketGame game) {
        this.gameEngine = engine;
        this.mainGame = game;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public boolean isUpPressed() { return isKeyPressed(KeyEvent.VK_UP); }
    public boolean isDownPressed() { return isKeyPressed(KeyEvent.VK_DOWN); }
    public boolean isLeftPressed() { return isKeyPressed(KeyEvent.VK_LEFT); }
    public boolean isRightPressed() { return isKeyPressed(KeyEvent.VK_RIGHT); }
    public boolean isSpacePressed() { return isKeyPressed(KeyEvent.VK_SPACE); }

    // Player 2 controls
    public boolean isWPressed() { return isKeyPressed(KeyEvent.VK_W); }
    public boolean isSPressed() { return isKeyPressed(KeyEvent.VK_S); }
    public boolean isAPressed() { return isKeyPressed(KeyEvent.VK_A); }
    public boolean isDPressed() { return isKeyPressed(KeyEvent.VK_D); }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        System.out.println("Mouse clicked at: " + mx + ", " + my);
        System.out.println("GameEngine null? " + (gameEngine == null));

        // 1. تحقق أولاً من حالة نهاية المستوى الثالث
        if (gameEngine != null && gameEngine.isLevelThreeComplete()) {
            System.out.println("Handling level 3 menu click");
            handleLevelThreeMenuMouseClick(e);
            return;
        }

        // 2. ثم تحقق من حالة Game Over العادية
        if (gameEngine != null && gameEngine.getGameState().isGameOver()) {
            System.out.println("Handling regular game over click");
            handleGameOverMouseClick(e);
            return;
        }
    }

    // دالة للتعامل مع نقرات قائمة نهاية المستوى الثالث
    private void handleLevelThreeMenuMouseClick(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        System.out.println("Level 3 menu click at: " + mx + ", " + my);

        // أبعاد الأزرار من GameRenderer
        int buttonWidth = GameRenderer.BUTTON_WIDTH;
        int buttonHeight = GameRenderer.BUTTON_HEIGHT;

        // إحداثيات الأزرار
        int playAgainX = GameRenderer.PLAY_AGAIN_BTN_X;
        int playAgainY = GameRenderer.PLAY_AGAIN_BTN_Y;

        int homeX = GameRenderer.HOME_BTN_X;
        int homeY = GameRenderer.HOME_BTN_Y;

        int exitX = GameRenderer.EXIT_BTN_X;
        int exitY = GameRenderer.EXIT_BTN_Y;

        // تحقق من النقر على زر Play Again
        if (mx >= playAgainX && mx <= playAgainX + buttonWidth &&
                my >= playAgainY && my <= playAgainY + buttonHeight) {
            System.out.println("Play Again clicked via mouse");
            // اختيار الزر الأول في القائمة
            gameEngine.handleLevelThreeMenuSelection(0);
            return;
        }

        // تحقق من النقر على زر Back to Home
        if (mx >= homeX && mx <= homeX + buttonWidth &&
                my >= homeY && my <= homeY + buttonHeight) {
            System.out.println("Back to Home clicked via mouse");
            // اختيار الزر الثاني في القائمة
            gameEngine.handleLevelThreeMenuSelection(1);
            return;
        }

        // تحقق من النقر على زر Exit Game
        if (mx >= exitX && mx <= exitX + buttonWidth &&
                my >= exitY && my <= exitY + buttonHeight) {
            System.out.println("Exit Game clicked via mouse");
            // اختيار الزر الثالث في القائمة
            gameEngine.handleLevelThreeMenuSelection(2);
            return;
        }
    }

    // دالة للتعامل مع نقرات شاشة Game Over العادية
    private void handleGameOverMouseClick(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        // الأزرار القديمة من GameRenderer
        int buttonWidth = GameRenderer.BUTTON_WIDTH;
        int buttonHeight = GameRenderer.BUTTON_HEIGHT;

        // إحداثيات الزر القديم Play Again (موجود في drawTwoButtons)
        int restartX = GameRenderer.RESTART_BTN_X;
        int restartY = GameRenderer.RESTART_BTN_Y;

        // إحداثيات الزر القديم Exit Game
        int exitX = GameRenderer.MENU_BTN_X;
        int exitY = GameRenderer.MENU_BTN_Y;

        // زر Play Again في Game Over العادي
        if (mx >= restartX && mx <= restartX + buttonWidth &&
                my >= restartY && my <= restartY + buttonHeight) {
            System.out.println("Restart Clicked!");
            gameEngine.resetGame();
        }

        // زر Exit في Game Over العادي
        if (mx >= exitX && mx <= exitX + buttonWidth &&
                my >= exitY && my <= exitY + buttonHeight) {
            System.out.println("Exit Clicked!");
            System.exit(0);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    public boolean isMouseButtonPressed() { return mousePressed; }
}