package RocketGame.Entities;

import javax.media.opengl.GL;

import RocketGame.Entities.GameObject;
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
    }

    @Override
    public void update(float deltaTime) {

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

    @Override
    public void render(GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(position.x, position.y, 0);


        drawShieldEffect(gl);

        drawRocketBody(gl);

        drawFlames(gl);

        gl.glPopMatrix();
    }

    private void drawShieldEffect(GL gl) {
        if (shield <= 0) return;

        float shieldStrength = (float) shield / maxShield;
        float centerX = width / 2;
        float centerY = height / 2;

        long time = System.currentTimeMillis();
        float pulse = (float) Math.sin(time / 200.0) * 3;

        for (int ring = 0; ring < 2; ring++) {
            float radius = 32 + ring * 6 + pulse;
            float brightness = shieldStrength * (1.0f - ring * 0.4f);

            gl.glColor3f(0.3f * brightness, 0.7f * brightness, brightness );
            gl.glLineWidth(2.0f);

            gl.glBegin(GL.GL_LINE_LOOP);
            for (int degrees = 0; degrees < 360; degrees += 10) {
                double radians = Math.toRadians(degrees);
                float x = centerX + (float) Math.cos(radians) * radius;
                float y = centerY + (float) Math.sin(radians) * radius;
                gl.glVertex2f(x, y);
            }
            gl.glEnd();
        }
    }

    private void drawRocketBody(GL gl) {
        if (isInvincible() && (System.currentTimeMillis() / 100) % 2 == 0) {
            gl.glColor3f(1.0f, 1.0f, 1.0f); // White flash
        } else {
            gl.glColor3f(1.0f, 0.3f, 0.3f); // Red rocket
        }

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(10, 0);
        gl.glVertex2f(30, 0);
        gl.glVertex2f(30, 40);
        gl.glVertex2f(10, 40);
        gl.glEnd();

        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(20, -15);
        gl.glVertex2f(5, 0);
        gl.glVertex2f(35, 0);
        gl.glEnd();

        gl.glColor3f(0.3f, 0.3f, 1.0f);
        gl.glBegin(GL.GL_TRIANGLES);

        gl.glVertex2f(0, 30);
        gl.glVertex2f(10, 20);
        gl.glVertex2f(10, 40);

        gl.glVertex2f(40, 30);
        gl.glVertex2f(30, 20);
        gl.glVertex2f(30, 40);
        gl.glEnd();
    }

    private void drawFlames(GL gl) {
        float flameHeight = (float) (Math.random() * 10 + 15);

        gl.glColor3f(1.0f, 0.7f, 0.0f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(15, 40);
        gl.glVertex2f(12, 40 + flameHeight);
        gl.glVertex2f(20, 40);
        gl.glEnd();

        gl.glColor3f(1.0f, 0.3f, 0.0f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(25, 40);
        gl.glVertex2f(28, 40 + flameHeight);
        gl.glVertex2f(20, 40);
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