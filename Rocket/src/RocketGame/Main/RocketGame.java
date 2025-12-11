package RocketGame.Main;

import javax.media.opengl.GLCanvas;
import javax.swing.*;
import RocketGame.Core.GameEngine;
import RocketGame.Core.Home;
import RocketGame.Input.InputHandler;
import RocketGame.Rendering.GameRenderer;
import RocketGame.Util.Constants;
import com.sun.opengl.util.FPSAnimator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RocketGame extends JFrame {
    private GLCanvas glCanvas;
    private FPSAnimator animator;
    private GameEngine gameEngine;
    private GameRenderer gameRenderer;
    private InputHandler inputHandler;
    private Home homeScreen;

    public RocketGame() {
        super(Constants.WINDOW_TITLE);
        initializeComponents();
        configureWindow();
        start();
    }

    private void initializeComponents() {
        glCanvas = new GLCanvas();
        glCanvas.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        getContentPane().add(glCanvas, BorderLayout.CENTER);

        homeScreen = new Home(this);

        glCanvas.addGLEventListener(homeScreen);
        glCanvas.addMouseListener((MouseListener) homeScreen);

        animator = new FPSAnimator(glCanvas, 60);
    }

    public void startGame(boolean isMultiplayer , boolean isAI , int level , String username , String username2) {
        glCanvas.removeGLEventListener(homeScreen);
        glCanvas.removeMouseListener(homeScreen);

        gameEngine = new GameEngine(null , isMultiplayer , isAI , level , username , username2);

        inputHandler = new InputHandler(gameEngine, this);

        gameEngine.setInputHandler(inputHandler);

        gameRenderer = new GameRenderer(gameEngine);

        glCanvas.addGLEventListener(gameRenderer);
        glCanvas.addKeyListener(inputHandler);
        glCanvas.addMouseListener(inputHandler);
        glCanvas.addMouseMotionListener(inputHandler);

        glCanvas.requestFocusInWindow();
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stop();
                System.exit(0);
            }
        });
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
    public void goToMenu() {
        if (gameEngine != null) {
            gameEngine = null;
        }
        glCanvas.removeGLEventListener(gameRenderer);
        glCanvas.removeKeyListener(inputHandler);
        glCanvas.removeMouseListener(inputHandler);
        glCanvas.removeMouseMotionListener(inputHandler);

        homeScreen = new Home(this);

        glCanvas.addGLEventListener(homeScreen);
        glCanvas.addMouseListener(homeScreen);
        glCanvas.requestFocusInWindow();
    }

    public void start() {
        animator.start();
    }

    public void stop() {
        if (animator != null) {
            animator.stop();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RocketGame());
    }
}