package RocketGame.Core;

import RocketGame.Main.RocketGame;
import RocketGame.Rendering.AnimListener;
import RocketGame.Texture.TextureReader;
import RocketGame.Util.Constants;
import RocketGame.Util.UserStatsManager;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.swing.JOptionPane;

public class Home extends AnimListener implements MouseListener {

    private RocketGame game;
    private UserStatsManager statsManager;

    String textureNames[] = {"start.png", "instructions.png","exit.png",
            "single.png","multi.png","howtoplay.png","easy.png","medium.png",
            "hard.png","back.png","ai.png", "background.png"};

    TextureReader.Texture texture[] = new TextureReader.Texture[textureNames.length];

    int textures[] = new int[textureNames.length];

    int startX = 300;
    int startY = 200;
    int btnWidth = 200;
    int btnHeight = 60;
    int helpX = 300;
    int helpY = 280;
    int exitX = 300;
    int exitY = 360;
    int singleX = 300;
    int singleY = 300;
    int multiX = 300;
    int multiY = 220;
    int easyX = 300;
    int easyY = 220;
    int mediumX = 300;
    int mediumY = 300;
    int hardX = 300;
    int hardY = 380;
    int backX = 300;
    int backY = 460;
    int helpBackX = 300;
    int helpBackY = 500;
    int aiX = 300;
    int aiY = 380;

    boolean isLevelSelection = false;
    int selectedDifficulty = 1;
    boolean showHelp = false;
    boolean isSelectionMode = false;

    public Home(RocketGame game) {
        this.game = game;
        this.statsManager = new UserStatsManager();
    }

    public Home() {
        this.statsManager = new UserStatsManager();
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, 0, -1, 1);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glGenTextures(textureNames.length, textures, 0);

        for(int i = 0; i < textureNames.length; i++){
            try {
                texture[i] = TextureReader.readTexture(assetsFolderName + "//" + textureNames[i] ,
                        true);
                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
                new GLU().gluBuild2DMipmaps(
                        GL.GL_TEXTURE_2D, GL.GL_RGBA,
                        texture[i].getWidth(), texture[i].getHeight(),
                        GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
                        texture[i].getPixels()
                );
            } catch( IOException e ) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

        System.out.println("\n=== GAME STARTED ===");
        statsManager.printLeaderboard();
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        drawTexture(gl, textureNames.length - 1, 0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        if (!showHelp && !isSelectionMode && !isLevelSelection) {
            drawTexture(gl, 0, startX, startY, btnWidth, btnHeight);
            drawTexture(gl, 1, helpX, helpY, btnWidth, btnHeight);
            drawTexture(gl , 2 , exitX, exitY, btnWidth, btnHeight);
        } else if (isLevelSelection) {
            drawTexture(gl, 6, easyX, easyY, btnWidth, btnHeight);
            drawTexture(gl, 7, mediumX, mediumY, btnWidth, btnHeight);
            drawTexture(gl, 8, hardX, hardY, btnWidth, btnHeight);
            drawTexture(gl, 9, backX, backY, btnWidth, btnHeight);
        } else if (isSelectionMode) {
            drawTexture(gl, 3, singleX, singleY, btnWidth, btnHeight);
            drawTexture(gl, 4, multiX, multiY, btnWidth, btnHeight);
            drawTexture(gl, 9, backX, backY, btnWidth, btnHeight);
            drawTexture(gl, 10, aiX, aiY, btnWidth, btnHeight);
        }else {
            drawTexture(gl , 5 , 0 , 0 , 800, 600);
        }
    }

    public void drawTexture(GL gl, int textureIndex, float x, float y, float width, float height){
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[textureIndex]);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex2f(x, y + height);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex2f(x + width, y + height);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex2f(x + width, y);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex2f(x, y);
        gl.glEnd();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int width, int height) {}

    @Override
    public void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1) {}

    private String requestUsername(String prompt) {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    null,
                    prompt,
                    "Rocket Game",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) {
                System.out.println("cancel pressed  => to home page");
                return null;
            }

            if (input.trim().isEmpty()) {
                int result = JOptionPane.showConfirmDialog(
                        null,
                        "No Name Entered try again",
                        "No Name Entered",
                        JOptionPane.YES_NO_OPTION
                );

                if (result == JOptionPane.YES_OPTION) {
                    continue;
                } else {
                    return null;
                }
            }

            return input.trim();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        boolean backClicked = false;

        if (isLevelSelection || isSelectionMode) {
            if (mx >= backX && mx <= backX + btnWidth && my >= backY && my <= backY + btnHeight) {
                backClicked = true;
            }
        } else if (showHelp) {
            if (mx >= helpBackX && mx <= helpBackX + btnWidth && my >= helpBackY && my <= helpBackY + btnHeight) {
                backClicked = true;
            }
        }

        if (backClicked) {
            isLevelSelection = false;
            isSelectionMode = false;
            showHelp = false;
            return;
        }

        if (isSelectionMode){
            if (mx >= singleX && mx <= singleX + btnWidth && my >= singleY && my <= singleY + btnHeight) {
                System.out.println("Single Player Selected!");

                String name = requestUsername("Enter Name : ");
                if (name == null) {
                    return;
                }

                if (game != null) {
                    game.startGame(false , false , selectedDifficulty , name , null);
                }
                return;
            }

            if (mx >= multiX && mx <= multiX + btnWidth && my >= multiY && my <= multiY + btnHeight) {
                System.out.println("Multi Player Selected!");

                String name1 = requestUsername("Enter 1-st player : ");
                if (name1 == null) {
                    return;
                }

                String name2 = requestUsername("Enter 2-nd player");
                if (name2 == null) {
                    return;
                }

                if (game != null) {
                    game.startGame(true , false , selectedDifficulty , name1 , name2);
                }
                return;
            }


            if (mx >= aiX && mx <= aiX + btnWidth && my >= 360 && my <= 360 + btnHeight) {
                System.out.println("AI Mode Selected!");

                String name = requestUsername("Enter Name : ");
                if (name == null) {
                    return;
                }

                if (game != null) {
                    game.startGame(true, true, selectedDifficulty , name , "AI");
                }
                return;
            }

        } else if (isLevelSelection) {
            if (mx >= easyX && mx <= easyX + btnWidth && my >= easyY && my <= easyY + btnHeight) {
                selectedDifficulty = 1;
                isLevelSelection = false;
                isSelectionMode = true;
            }

            if (mx >= mediumX && mx <= mediumX + btnWidth && my >= mediumY && my <= mediumY + btnHeight) {
                selectedDifficulty = 2;
                isLevelSelection = false;
                isSelectionMode = true;
            }

            if (mx >= hardX && mx <= hardX + btnWidth && my >= hardY && my <= hardY + btnHeight) {
                selectedDifficulty = 3;
                isLevelSelection = false;
                isSelectionMode = true;
            }

        } else if (!showHelp) {
            if (mx >= startX && mx <= startX + btnWidth && my >= startY && my <= startY + btnHeight) {
                System.out.println("Start Game Clicked!");
                isSelectionMode = false;
                isLevelSelection = true;
            }

            if (mx >= helpX && mx <= helpX + btnWidth && my >= helpY && my <= helpY + btnHeight) {
                System.out.println("Help Clicked!");
                showHelp = true;
            }

            if (mx >= exitX && mx <= exitX + btnWidth && my >= exitY && my <= exitY + btnHeight){
                System.out.println("Exit Game Clicked!");
                System.exit(0);
            }
        } else {
            showHelp = false;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}