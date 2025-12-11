package RocketGame.Entities;

import javax.media.opengl.GL;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import RocketGame.Util.Vector2D;

public class Boss extends GameObject {
    private int health;
    private int maxHealth;
    private float speed;
    private int phase;
    private float direction;
    private long lastShot;
    private int shootInterval;
    private BossState state;
    private float targetY;
    private long phaseChangeTime;

    private static Texture bossTexture;
    private static boolean textureLoaded = false;

    public enum BossState {
        ENTERING, ATTACKING, DAMAGED, DEFEATED
    }

    public Boss(float x, float y) {
        super(x, y, 200, 200);

        this.health = RocketGame.Util.Constants.BOSS_HEALTH;
        this.maxHealth = RocketGame.Util.Constants.BOSS_HEALTH;

        this.speed = 2.0f;
        this.phase = 1;
        this.direction = 1;
        this.lastShot = System.currentTimeMillis();
        this.shootInterval = 800;
        this.state = BossState.ENTERING;
        this.targetY = 50;
        this.phaseChangeTime = 0;

        this.velocity.set(0, speed);

        if (!textureLoaded) {
            loadBossTexture();
        }
    }

    private void loadBossTexture() {
        try {
            File textureFile = new File("Assets/37.png");
            if (textureFile.exists()) {
                bossTexture = TextureIO.newTexture(textureFile, true);
                bossTexture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
                bossTexture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
                textureLoaded = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(float deltaTime) {
        switch (state) {
            case ENTERING:
                position.y += speed;
                if (position.y >= targetY) {
                    position.y = targetY;
                    state = BossState.ATTACKING;
                }
                break;

            case ATTACKING:
                position.x += direction * speed * 2;
                if (position.x <= 0) direction = 1;
                else if (position.x >= 800 - width) direction = -1;

                updatePhase();
                break;

            case DAMAGED:
                position.x += direction * speed * 2;
                if (position.x <= 0) direction = 1;
                else if (position.x >= 800 - width) direction = -1;

                updatePhase();
                if (System.currentTimeMillis() - phaseChangeTime > 200) {
                    state = BossState.ATTACKING;
                }
                break;

            case DEFEATED:
                break;
        }
    }

    private void updatePhase() {
        float healthPercent = (float) health / maxHealth;
        if (healthPercent <= 0.33f) {
            phase = 3;
            shootInterval = 500;
        } else if (healthPercent <= 0.66f) {
            phase = 2;
            shootInterval = 750;
        }
    }

    public boolean canShoot() {
        if (state != BossState.ATTACKING) return false;
        long now = System.currentTimeMillis();
        if (now - lastShot >= shootInterval) {
            lastShot = now;
            return true;
        }
        return false;
    }

    public List<Vector2D> getBulletSpawnPositions() {
        List<Vector2D> positions = new ArrayList<>();
        positions.add(new Vector2D(position.x + width / 2, position.y + height / 2));

        if (phase >= 2) {
            positions.add(new Vector2D(position.x + width * 0.2f, position.y + height * 0.7f));
            positions.add(new Vector2D(position.x + width * 0.8f, position.y + height * 0.7f));
        }
        return positions;
    }

    public void takeDamage(int amount) {
        health -= amount;
        state = BossState.DAMAGED;
        phaseChangeTime = System.currentTimeMillis();
        if (health <= 0) {
            health = 0;
            state = BossState.DEFEATED;
        }
    }

    @Override
    public void render(GL gl) {
        if (state == BossState.DEFEATED)return;

        gl.glPushMatrix();
        gl.glTranslatef(position.x, position.y, 0);

        if (state == BossState.DAMAGED && (System.currentTimeMillis() / 50) % 2 == 0) {
            gl.glColor3f(1.0f, 0.5f, 0.5f);
        } else {
            gl.glColor3f(1.0f, 1.0f, 1.0f);
        }

        if (textureLoaded && bossTexture != null) {
            bossTexture.bind();
            bossTexture.enable();
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

            gl.glBegin(GL.GL_QUADS);
            gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(0, 0);
            gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(width, 0);
            gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(width, height);
            gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(0, height);
            gl.glEnd();

            bossTexture.disable();
            gl.glDisable(GL.GL_TEXTURE_2D);
        } else {
            gl.glColor3f(0.8f, 0.4f, 0.0f);
            drawCircle(gl, width/2, height/2, width/2);
        }

        drawBossHealthBar(gl);
        gl.glPopMatrix();
    }

    private void drawBossHealthBar(GL gl) {
        float barWidth = 400;
        float barHeight = 15;
        float barX = -position.x + (800 - barWidth) / 2;
        float barY = -position.y + 20;


        gl.glColor3f(0.3f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY); gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight); gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();


        float healthPercent = (float) health / maxHealth;
        gl.glColor3f(1.0f - healthPercent, healthPercent, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY); gl.glVertex2f(barX + barWidth * healthPercent, barY);
        gl.glVertex2f(barX + barWidth * healthPercent, barY + barHeight); gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();
    }

    private void drawCircle(GL gl, float cx, float cy, float r) {
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        gl.glVertex2f(cx, cy);
        for (int i = 0; i <= 360; i+=10) {
            double angle = Math.toRadians(i);
            gl.glVertex2f(cx + (float)Math.cos(angle)*r, cy + (float)Math.sin(angle)*r);
        }
        gl.glEnd();
    }


    public int getHealth() { return health; }
    public boolean isDefeated() { return state == BossState.DEFEATED; }
    public boolean isActive() {
        return state == BossState.ATTACKING || state == BossState.DAMAGED;
    }}