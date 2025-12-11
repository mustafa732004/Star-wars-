package RocketGame.Main;

import javax.media.opengl.GLCanvas;
import javax.swing.*;


import RocketGame.Core.GameEngine;
import RocketGame.Input.InputHandler;
import RocketGame.Rendering.GameRenderer;
import RocketGame.Util.Constants;
import com.sun.opengl.util.FPSAnimator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RocketGame extends JFrame {
    private GLCanvas glCanvas;
    private FPSAnimator animator;
    private GameEngine gameEngine;
    private GameRenderer gameRenderer;
    private InputHandler inputHandler;


    public RocketGame() {
        super( Constants.WINDOW_TITLE);

        initializeComponents();

        configureWindow();

        start();
    }

    private void initializeComponents() {
        inputHandler = new InputHandler();

        gameEngine = new GameEngine(inputHandler);

        gameRenderer = new GameRenderer(gameEngine);

        glCanvas = new GLCanvas();
        glCanvas.addGLEventListener(gameRenderer);
        glCanvas.addKeyListener(inputHandler);
        glCanvas.addMouseListener(inputHandler);

        glCanvas.setPreferredSize(new Dimension(
                Constants.WINDOW_WIDTH,
                Constants.WINDOW_HEIGHT
        ));

        getContentPane().add(glCanvas, BorderLayout.CENTER);

        animator = new FPSAnimator(glCanvas, 60);
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

        glCanvas.requestFocus();
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
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                new RocketGame();
            }
        });
    }
}