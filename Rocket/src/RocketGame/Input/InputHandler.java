package RocketGame.Input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

public class InputHandler implements KeyListener, MouseListener {
    private final Set<Integer> pressedKeys;
    private final Set<Integer> clickedButtons;
    private int mouseX, mouseY;

    public InputHandler() {
        pressedKeys = new HashSet<>();
        clickedButtons = new HashSet<>();
        mouseX = 0;
        mouseY = 0;
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
    public void keyTyped(KeyEvent e) {
    }

    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public boolean isUpPressed() {
        return isKeyPressed(KeyEvent.VK_UP) || isKeyPressed(KeyEvent.VK_W);
    }

    public boolean isDownPressed() {
        return isKeyPressed(KeyEvent.VK_DOWN) || isKeyPressed(KeyEvent.VK_S);
    }

    public boolean isLeftPressed() {
        return isKeyPressed(KeyEvent.VK_LEFT) || isKeyPressed(KeyEvent.VK_A);
    }

    public boolean isRightPressed() {
        return isKeyPressed(KeyEvent.VK_RIGHT) || isKeyPressed(KeyEvent.VK_D);
    }

    public boolean isSpacePressed() {
        return isKeyPressed(KeyEvent.VK_SPACE);
    }

    public boolean isEscapePressed() {
        return isKeyPressed(KeyEvent.VK_ESCAPE);
    }

    public boolean isMPressed() {
        return isKeyPressed(KeyEvent.VK_M);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        clickedButtons.add(e.getButton());
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        clickedButtons.add(e.getButton());
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        clickedButtons.remove(e.getButton());
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    public boolean isMouseClicked(int button) {
        return clickedButtons.contains(button);
    }

    public void clearMouseClicks() {
        clickedButtons.clear();
    }

    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }

    public void clearAll() {
        pressedKeys.clear();
        clickedButtons.clear();
    }
}