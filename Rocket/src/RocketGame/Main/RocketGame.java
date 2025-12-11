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
        glCanvas.addMouseListener(homeScreen);

        animator = new FPSAnimator(glCanvas, 60);
    }

    // دالة الانتقال من القائمة إلى اللعبة مع بارامتر وضع اللاعبين
    public void startGame(boolean twoPlayerMode) {
        System.out.println("Starting game with twoPlayerMode: " + twoPlayerMode);

        glCanvas.removeGLEventListener(homeScreen);
        glCanvas.removeMouseListener(homeScreen);

        gameEngine = new GameEngine(null);

        // تهيئة GameState بوضع اللاعبين الصحيح
        gameEngine.getGameState().initialize(false, twoPlayerMode);

        inputHandler = new InputHandler(gameEngine, this);

        gameEngine.setInputHandler(inputHandler);

        gameRenderer = new GameRenderer(gameEngine);

        glCanvas.addGLEventListener(gameRenderer);
        glCanvas.addKeyListener(inputHandler);
        glCanvas.addMouseListener(inputHandler);
        glCanvas.addMouseMotionListener(inputHandler);

        glCanvas.requestFocusInWindow();
    }

    // نسخة إضافية من startGame تأخذ GameEngine موجود
    public void startGame(boolean twoPlayerMode, GameEngine existingEngine) {
        System.out.println("Starting game with existing engine");

        glCanvas.removeGLEventListener(homeScreen);
        glCanvas.removeMouseListener(homeScreen);

        // استخدم GameEngine الموجود
        gameEngine = existingEngine;

        // تأكد من تهيئة GameState
        if (gameEngine.getGameState() == null) {
            gameEngine.getGameState().initialize(false, twoPlayerMode);
        }

        inputHandler = new InputHandler(gameEngine, this);
        gameEngine.setInputHandler(inputHandler);

        gameRenderer = new GameRenderer(gameEngine);

        glCanvas.addGLEventListener(gameRenderer);
        glCanvas.addKeyListener(inputHandler);
        glCanvas.addMouseListener(inputHandler);
        glCanvas.addMouseMotionListener(inputHandler);

        glCanvas.requestFocusInWindow();
    }

    // دالة جديدة للعودة إلى القائمة الرئيسية
    public void returnToMainMenu() {
        System.out.println("Returning to Main Menu...");

        // تنظيف اللعبة الحالية
        if (gameRenderer != null) {
            glCanvas.removeGLEventListener(gameRenderer);
        }
        if (inputHandler != null) {
            glCanvas.removeKeyListener(inputHandler);
            glCanvas.removeMouseListener(inputHandler);
            glCanvas.removeMouseMotionListener(inputHandler);
        }

        // إعادة تعيين المراجع
        gameEngine = null;
        gameRenderer = null;
        inputHandler = null;

        // إعادة تهيئة الشاشة الرئيسية
        if (homeScreen == null) {
            homeScreen = new Home(this);
        }

        // إضافة مستمعي الأحداث للشاشة الرئيسية
        glCanvas.addGLEventListener(homeScreen);
        glCanvas.addMouseListener(homeScreen);

        // طلب التركيز
        glCanvas.requestFocusInWindow();

        // إعادة الرسم
        glCanvas.repaint();
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
        }
        glCanvas.removeGLEventListener(gameRenderer);
        glCanvas.removeKeyListener(inputHandler);
        glCanvas.removeMouseListener(inputHandler);

        if (homeScreen == null) {
            homeScreen = new Home(this);
        }

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