package RocketGame.Entities;

import javax.media.opengl.GL;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.IOException;
import RocketGame.Util.Constants;

public class Bullet extends GameObject {
    private int damage;
    private float speed;
    private boolean fromEnemy;
    private String bulletType;
    private float[] color;
    private int playerNumber; // إضافة هذا المتغير الجديد

    private static Texture planetBulletTexture;
    private static boolean textureLoaded = false;

    public Bullet(float x, float y, int damage, boolean isEnemyBullet, String type) {
        super(x, y, 30, 30);

        this.damage = damage;
        this.speed = Constants.BULLET_SPEED * 0.6f;
        this.fromEnemy = isEnemyBullet;
        this.bulletType = type;
        this.playerNumber = 1; // القيمة الافتراضية

        if (bulletType.equals("planet")) {
            this.color = new float[]{1.0f, 1.0f, 1.0f};
            this.velocity.set(0, speed);

            if (!textureLoaded) loadPlanetTexture();
        } else {
            this.color = new float[]{1.0f, 0.0f, 0.0f};
            this.velocity.set(0, speed);
        }
    }

    public Bullet(float x, float y, int damage, String bulletType) {
        super(x, y, Constants.BULLET_WIDTH, Constants.BULLET_HEIGHT);
        this.damage = damage;
        this.speed = Constants.BULLET_SPEED;
        this.fromEnemy = false;
        this.bulletType = bulletType;
        this.playerNumber = 1; // القيمة الافتراضية

        // Laser settings
        if (bulletType.equals("laser")) {
            this.color = new float[]{0.0f, 1.0f, 1.0f}; // Cyan
            this.width = 4;
            this.height = 30;
        } else {
            this.color = new float[]{0.0f, 1.0f, 0.0f}; // Green
        }

        this.velocity.set(0, -speed);
    }

    public Bullet(float x, float y, int damage, boolean isEnemyBullet) {
        this(x, y, damage, isEnemyBullet, "normal");
        this.width = Constants.BULLET_WIDTH;
        this.height = Constants.BULLET_HEIGHT;
        this.playerNumber = 1; // القيمة الافتراضية
    }

    public Bullet(float x, float y, int damage, float angle) {
        super(x, y, Constants.BULLET_WIDTH, Constants.BULLET_HEIGHT);

        this.damage = damage;
        this.speed = Constants.BULLET_SPEED;
        this.fromEnemy = false;
        this.bulletType = "spread";
        this.color = new float[]{0.0f, 1.0f, 0.0f}; // Green
        this.playerNumber = 1; // القيمة الافتراضية

        float radians = (float) Math.toRadians(angle);
        velocity.set((float) Math.sin(radians) * speed,
                -(float) Math.cos(radians) * speed);
    }

    private void loadPlanetTexture() {
        try {
            File f = new File("Assets/planet5.png");
            if(f.exists()) {
                planetBulletTexture = TextureIO.newTexture(f, true);
                textureLoaded = true;
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void update(float deltaTime) {
        position.x += velocity.x * deltaTime;
        position.y += velocity.y * deltaTime;
    }

    @Override
    public void render(GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(position.x, position.y, 0);

        if ("planet".equals(bulletType) && textureLoaded) {
            planetBulletTexture.bind();
            planetBulletTexture.enable();
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

            gl.glColor3f(1,1,1);
            gl.glBegin(GL.GL_QUADS);
            gl.glTexCoord2f(0, 0); gl.glVertex2f(0, 0);
            gl.glTexCoord2f(1, 0); gl.glVertex2f(width, 0);
            gl.glTexCoord2f(1, 1); gl.glVertex2f(width, height);
            gl.glTexCoord2f(0, 1); gl.glVertex2f(0, height);
            gl.glEnd();

            planetBulletTexture.disable();
            gl.glDisable(GL.GL_TEXTURE_2D);
        } else {
            // استخدام اللون المخزن في المتغير color
            gl.glColor3f(color[0], color[1], color[2]);

            if (bulletType.equals("laser")) {
                gl.glBegin(GL.GL_QUADS);
                gl.glVertex2f(0, 0); gl.glVertex2f(width, 0);
                gl.glVertex2f(width, height); gl.glVertex2f(0, height);
                gl.glEnd();
            } else {
                gl.glBegin(GL.GL_QUADS);
                gl.glVertex2f(0, 0); gl.glVertex2f(width, 0);
                gl.glVertex2f(width, height); gl.glVertex2f(0, height);
                gl.glEnd();
            }
        }

        gl.glPopMatrix();
    }

    public boolean isOffScreen(int sw, int sh) {
        return position.y > sh || position.y < -height;
    }

    // ==================== إضافة الدوال المفقودة ====================

    // Getter و Setter لـ playerNumber
    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    // Getter و Setter لـ color
    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    // دالة مساعدة لتعيين لون بسهولة
    public void setColor(float r, float g, float b) {
        this.color = new float[]{r, g, b};
    }

    // Getter و Setter لـ bulletType
    public String getBulletType() {
        return bulletType;
    }

    public void setBulletType(String bulletType) {
        this.bulletType = bulletType;
    }

    // Getter و Setter لـ damage
    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    // Getter و Setter لـ speed
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    // Getter و Setter لـ fromEnemy
    public boolean isFromEnemy() {
        return fromEnemy;
    }

    public void setFromEnemy(boolean fromEnemy) {
        this.fromEnemy = fromEnemy;
    }

    // ==================== نهاية الإضافات ====================
}