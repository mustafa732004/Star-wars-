package RocketGame.Entities;

import javax.media.opengl.GL;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.IOException;

import RocketGame.Util.Constants;
import RocketGame.Util.Vector2D;

public class Rocket extends GameObject {
    private int health;
    private final int maxHealth;
    private int shield;
    private final int maxShield;
    private float speed;
    private long lastShot;
    private int fireRate;
    private String weaponType;
    private int damage;
    private long invincibleUntil;

    private Texture rocketTexture;
    private boolean textureLoaded;

    public Rocket(float x, float y) {
        super(x, y, Constants.ROCKET_WIDTH, Constants.ROCKET_HEIGHT);

        this.health = Constants.ROCKET_MAX_HEALTH;
        this.maxHealth = Constants.ROCKET_MAX_HEALTH;
        this.shield = 0;
        this.maxShield = Constants.ROCKET_MAX_SHIELD;
        this.speed = Constants.ROCKET_SPEED;
        this.lastShot = 0;
        this.fireRate = 250;
        this.weaponType = "normal";
        this.damage = 1;
        this.invincibleUntil = 0;

        this.textureLoaded = false;
        this.rocketTexture = null;
    }

    @Override
    public void update(float deltaTime) {
        // لا شيء مطلوب هنا للصورة
    }

    public void move(float dx, float dy, int screenWidth, int screenHeight) {
        position.x += dx;
        position.y += dy;

        if (position.x < 0) position.x = 0;
        if (position.x > screenWidth - width) position.x = screenWidth - width;
        if (position.y < 0) position.y = 0;
        if (position.y > screenHeight - height) position.y = screenHeight - height;
    }

    public boolean canShoot() {
        return System.currentTimeMillis() - lastShot >= fireRate;
    }

    public void shoot() {
        lastShot = System.currentTimeMillis();
    }

    public void takeDamage(int amount) {
        if (isInvincible()) return;

        if (shield > 0) {
            shield = Math.max(0, shield - amount);
        } else {
            health = Math.max(0, health - amount);
        }
    }

    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public void addShield(int amount) {
        shield = Math.min(maxShield, shield + amount);
    }

    public boolean isInvincible() {
        return System.currentTimeMillis() < invincibleUntil;
    }

    public void setInvincible(int duration) {
        invincibleUntil = System.currentTimeMillis() + duration;
    }

    public void loadTexture(GL gl) {
        if (textureLoaded) return;

        try {
            String texturePath = "Assets/rocket.png";
            File textureFile = new File(texturePath);

            if (textureFile.exists()) {
                rocketTexture = TextureIO.newTexture(textureFile, true);
                rocketTexture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
                rocketTexture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
                textureLoaded = true;
                System.out.println("Rocket texture loaded successfully: " + texturePath);
            } else {
                System.err.println("Rocket texture file not found: " + texturePath);
                textureLoaded = false;
            }
        } catch (IOException e) {
            System.err.println("Error loading rocket texture: " + e.getMessage());
            textureLoaded = false;
        }
    }

    @Override
    public void render(GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(position.x, position.y, 0);

        drawShieldEffect(gl);

        if (!textureLoaded) {
            loadTexture(gl);
        }

        drawRocketWithTexture(gl);

        gl.glPopMatrix();
    }

    private void drawShieldEffect(GL gl) {
        if (shield <= 0) return;

        float shieldStrength = (float) shield / maxShield;

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        float glowSize = 10 * shieldStrength;
        gl.glColor4f(0.3f, 0.7f, 1.0f, shieldStrength * 0.5f);

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-glowSize, -glowSize);
        gl.glVertex2f(width + glowSize, -glowSize);
        gl.glVertex2f(width + glowSize, height + glowSize);
        gl.glVertex2f(-glowSize, height + glowSize);
        gl.glEnd();

        gl.glDisable(GL.GL_BLEND);
    }

    private void drawRocketWithTexture(GL gl) {
        if (rocketTexture != null && textureLoaded) {
            rocketTexture.bind();
            rocketTexture.enable();

            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

            // تأثير الوامض عندما يكون الصاروخ غير قابل للإصابة
            if (isInvincible() && (System.currentTimeMillis() / 100) % 2 == 0) {
                gl.glColor3f(1.0f, 1.0f, 1.0f);
            } else {
                gl.glColor3f(1.0f, 1.0f, 1.0f);
            }

            gl.glBegin(GL.GL_QUADS);

            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex2f(0, 0);

            gl.glTexCoord2f(1.0f, 0.0f);
            gl.glVertex2f(width, 0);

            gl.glTexCoord2f(1.0f, 1.0f);
            gl.glVertex2f(width, height);

            gl.glTexCoord2f(0.0f, 1.0f);
            gl.glVertex2f(0, height);

            gl.glEnd();

            rocketTexture.disable();
            gl.glDisable(GL.GL_TEXTURE_2D);
        } else {
            // النسخة الاحتياطية إذا لم يتم تحميل الصورة
            drawRocketBodyAsBackup(gl);
        }
    }

    private void drawRocketBodyAsBackup(GL gl) {
        // هذه النسخة الاحتياطية لا تستخدم المستطيل بل شكل الصاروخ القديم
        if (isInvincible() && (System.currentTimeMillis() / 100) % 2 == 0) {
            gl.glColor3f(1.0f, 1.0f, 1.0f);
        } else {
            gl.glColor3f(1.0f, 0.3f, 0.3f);
        }

        // جسم الصاروخ (مثلث)
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(width / 2, 0); // قمة الصاروخ
        gl.glVertex2f(0, height);    // الزاوية اليسرى السفلية
        gl.glVertex2f(width, height); // الزاوية اليمنى السفلية
        gl.glEnd();

        // قاعدة الصاروخ
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(width * 0.3f, height);
        gl.glVertex2f(width * 0.7f, height);
        gl.glVertex2f(width * 0.6f, height * 1.2f);
        gl.glVertex2f(width * 0.4f, height * 1.2f);
        gl.glEnd();

        // النوافذ/التفاصيل
        gl.glColor3f(0.3f, 0.3f, 1.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(width * 0.4f, height * 0.4f);
        gl.glVertex2f(width * 0.6f, height * 0.4f);
        gl.glVertex2f(width * 0.6f, height * 0.6f);
        gl.glVertex2f(width * 0.4f, height * 0.6f);
        gl.glEnd();
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public float getHealthPercent() {
        return (float) health / maxHealth;
    }

    public int getShield() {
        return shield;
    }

    public int getMaxShield() {
        return maxShield;
    }

    public float getShieldPercent() {
        return (float) shield / maxShield;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getFireRate() {
        return fireRate;
    }

    public void setFireRate(int fireRate) {
        this.fireRate = fireRate;
    }

    public String getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(String weaponType) {
        this.weaponType = weaponType;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public boolean isDead() {
        return health <= 0;
    }
}