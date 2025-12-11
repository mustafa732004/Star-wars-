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

    private GameEngine gameEngine;
    private RocketGame mainGame;

    private boolean mousePressed = false;

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

    public boolean isWPressed() { return isKeyPressed(KeyEvent.VK_W); }
    public boolean isSPressed() { return isKeyPressed(KeyEvent.VK_S); }
    public boolean isAPressed() { return isKeyPressed(KeyEvent.VK_A); }
    public boolean isDPressed() { return isKeyPressed(KeyEvent.VK_D); }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (gameEngine == null) return;
        int mx = e.getX();
        int my = e.getY();

        if (gameEngine.getGameState().isGameOver()) {
            if (mx >= GameRenderer.RESTART_BTN_X && mx <= GameRenderer.RESTART_BTN_X + GameRenderer.BUTTON_WIDTH &&
                    my >= GameRenderer.RESTART_BTN_Y && my <= GameRenderer.RESTART_BTN_Y + GameRenderer.BUTTON_HEIGHT) {
                gameEngine.resetGame();
            }
            if (mx >= GameRenderer.MENU_BTN_X && mx <= GameRenderer.MENU_BTN_X + GameRenderer.BUTTON_WIDTH &&
                    my >= GameRenderer.MENU_BTN_Y && my <= GameRenderer.MENU_BTN_Y + GameRenderer.BUTTON_HEIGHT) {
                System.exit(0);
            }
            return;
        }

        if (gameEngine.getGameState().isPaused()) {
            if (mx >= GameRenderer.RESUME_BTN_X && mx <= GameRenderer.RESUME_BTN_X + GameRenderer.BUTTON_WIDTH &&
                    my >= GameRenderer.RESUME_BTN_Y && my <= GameRenderer.RESUME_BTN_Y + GameRenderer.BUTTON_HEIGHT) {
                gameEngine.getGameState().setPaused(false);
            }
            if (mx >= GameRenderer.MENU_BTN_X && mx <= GameRenderer.MENU_BTN_X + GameRenderer.BUTTON_WIDTH &&
                    my >= GameRenderer.MENU_BTN_Y && my <= GameRenderer.MENU_BTN_Y + GameRenderer.BUTTON_HEIGHT) {
                System.exit(0);
            }
            return;
        }

        if (!gameEngine.getGameState().isGameOver()) {
            if (mx >= GameRenderer.PAUSE_BTN_X && mx <= GameRenderer.PAUSE_BTN_X + GameRenderer.PAUSE_BTN_SIZE &&
                    my >= GameRenderer.PAUSE_BTN_Y && my <= GameRenderer.PAUSE_BTN_Y + GameRenderer.PAUSE_BTN_SIZE) {
                gameEngine.getGameState().setPaused(true);
            }
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

    public boolean isMouseButtonPressed() {
        return mousePressed;
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
}