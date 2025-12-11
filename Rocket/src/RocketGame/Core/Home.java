package RocketGame.Core;

import RocketGame.Main.RocketGame;
import RocketGame.Rendering.AnimListener;
import RocketGame.Texture.TextureReader;
import RocketGame.Util.Constants;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class Home extends AnimListener implements MouseListener {

    private RocketGame game;

    // ترتيب الصور: start.png, instructions.png, exit.png, background.png, single.png, multi.png, back.png, howtoplay.png
    String textureNames[] = {"start.png", "instructions.png", "exit.png", "background.png", "single.png", "multi.png", "back.png", "howtoplay.png"};
    TextureReader.Texture texture[] = new TextureReader.Texture[textureNames.length];
    int textures[] = new int[textureNames.length];

    // إحداثيات الأزرار في الصفحة الرئيسية
    int startX = Constants.WINDOW_WIDTH / 2 - 100; // 300
    int startY = 220;

    int helpX = Constants.WINDOW_WIDTH / 2 - 100;
    int helpY = 300;

    int exitX = Constants.WINDOW_WIDTH / 2 - 100;
    int exitY = 380;

    // إحداثيات الأزرار في قائمة اللاعبين
    int singleX = Constants.WINDOW_WIDTH / 2 - 100;
    int singleY = 200;
    int multiX = Constants.WINDOW_WIDTH / 2 - 100;
    int multiY = 280;

    // زر العودة في صفحة التعليمات وقائمة اللاعبين
    int backX = 50;
    int backY = Constants.WINDOW_HEIGHT - 80;

    int btnWidth = 200;
    int btnHeight = 60;
    int btnSpacing = 20; // مسافة بين الأزرار

    boolean showHelp = false;
    boolean showPlayerSelection = false;  // صفحة اختيار نوع اللعبة

    public Home(RocketGame game) {
        this.game = game;
    }

    public Home() {}

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

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
                texture[i] = TextureReader.readTexture(assetsFolderName + "//" + textureNames[i] , true);
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
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        // رسم الخلفية
        drawTexture(gl, 3, 0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        if (showHelp) {
            // شاشة التعليمات
            drawTexture(gl, 7, 0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT); // howtoplay.png
            drawTexture(gl, 6, backX, backY, btnWidth, btnHeight); // back.png
        } else if (showPlayerSelection) {
            // شاشة اختيار نوع اللعبة
            drawTexture(gl, 4, singleX, singleY, btnWidth, btnHeight); // single.png
            drawTexture(gl, 5, multiX, multiY, btnWidth, btnHeight); // multi.png
            drawTexture(gl, 6, backX, backY, btnWidth, btnHeight); // back.png
        } else {
            // الصفحة الرئيسية - الأزرار متناسقة مع بعض
            drawTexture(gl, 0, startX, startY, btnWidth, btnHeight);        // Start
            drawTexture(gl, 1, helpX, helpY, btnWidth, btnHeight);          // Instructions
            drawTexture(gl, 2, exitX, exitY, btnWidth, btnHeight);          // Exit
        }
    }

    // دالة مساعدة لرسم الصور بالإحداثيات
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
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int width, int height) {
    }

    @Override
    public void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1) {}

    @Override
    public void mouseClicked(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        if (showHelp) {
            // زر العودة في شاشة التعليمات
            if (mx >= backX && mx <= backX + btnWidth && my >= backY && my <= backY + btnHeight) {
                System.out.println("Back Clicked from Instructions!");
                showHelp = false;
            }
        } else if (showPlayerSelection) {
            // زر Single Player
            if (mx >= singleX && mx <= singleX + btnWidth && my >= singleY && my <= singleY + btnHeight) {
                System.out.println("Single Player Clicked!");
                if (game != null) {
                    game.startGame(false); // Single player
                }
            }

            // زر Multi Player
            if (mx >= multiX && mx <= multiX + btnWidth && my >= multiY && my <= multiY + btnHeight) {
                System.out.println("Multi Player Clicked!");
                if (game != null) {
                    game.startGame(true); // Multi player
                }
            }

            // زر العودة
            if (mx >= backX && mx <= backX + btnWidth && my >= backY && my <= backY + btnHeight) {
                System.out.println("Back Clicked from Player Selection!");
                showPlayerSelection = false;
            }
        } else {
            // Start Game
            if (mx >= startX && mx <= startX + btnWidth && my >= startY && my <= startY + btnHeight) {
                System.out.println("Start Game Clicked!");
                showPlayerSelection = true;
            }

            // Help
            if (mx >= helpX && mx <= helpX + btnWidth && my >= helpY && my <= helpY + btnHeight) {
                System.out.println("Help Clicked!");
                showHelp = true;
            }

            // Exit
            if (mx >= exitX && mx <= exitX + btnWidth && my >= exitY && my <= exitY + btnHeight){
                System.out.println("Exit Game Clicked!");
                System.exit(0);
            }
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