package RocketGame.Entities;

import javax.media.opengl.GL;
import RocketGame.Util.Vector2D;
import java.util.ArrayList;
import java.util.List;

public class Boss extends GameObject {
    private int health;
    private int maxHealth;
    private float speed;
    private int phase; // 1, 2, or 3
    private float direction; // -1 = left, 1 = right
    private long lastShot;
    private int shootInterval;
    private BossState state;
    private float targetY; // Where boss wants to move to
    private long phaseChangeTime;

    // Boss states
    public enum BossState {
        ENTERING,   // Moving into position
        ATTACKING,  // Active combat
        DAMAGED,    // Just took damage (flash)
        DEFEATED    // Destroyed
    }


    public Boss(float x, float y) {
        super(x, y, 160, 120);

        this.health = 50;
        this.maxHealth = 50;
        this.speed = 1.0f;
        this.phase = 1;
        this.direction = 1;
        this.lastShot = System.currentTimeMillis();
        this.shootInterval = 1000; // 1 second between shots
        this.state = BossState.ENTERING;
        this.targetY = 50; // Stop at Y=50
        this.phaseChangeTime = 0;

        this.velocity.set(0, speed);
    }

    @Override
    public void update(float deltaTime) {
        switch (state) {
            case ENTERING:
                updateEntering();
                break;
            case ATTACKING:
                updateAttacking(deltaTime);
                break;
            case DAMAGED:
                updateDamaged();
                break;
            case DEFEATED:
                // Boss is dead, do nothing
                break;
        }
    }

    private void updateEntering() {
        position.y += speed;

        if (position.y >= targetY) {
            position.y = targetY;
            state = BossState.ATTACKING;
        }
    }

    private void updateAttacking(float deltaTime) {
        position.x += direction * speed * 2;

        if (position.x <= 0) {
            direction = 1;
        } else if (position.x >= 800 - width) {
            direction = -1;
        }


        updatePhase();
    }


    private void updateDamaged() {
        long now = System.currentTimeMillis();
        if (now - phaseChangeTime > 200) { // Flash for 200ms
            state = BossState.ATTACKING;
        }
    }


    private void updatePhase() {
        float healthPercent = (float) health / maxHealth;

        if (healthPercent <= 0.33f && phase < 3) {
            phase = 3;
            shootInterval = 500; // Shoot faster
        } else if (healthPercent <= 0.66f && phase < 2) {
            phase = 2;
            shootInterval = 750;
        }
    }


    public boolean canShoot() {
        if (state != BossState.ATTACKING) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - lastShot >= shootInterval) {
            lastShot = now;
            return true;
        }
        return false;
    }


    public List<Vector2D> getBulletSpawnPositions() {
        List<Vector2D> positions = new ArrayList<>();

        switch (phase) {
            case 1:

                positions.add(new Vector2D(position.x + width / 2, position.y + height));
                break;

            case 2:

                positions.add(new Vector2D(position.x + width * 0.25f, position.y + height));
                positions.add(new Vector2D(position.x + width * 0.75f, position.y + height));
                break;

            case 3:

                positions.add(new Vector2D(position.x + width * 0.2f, position.y + height));
                positions.add(new Vector2D(position.x + width * 0.5f, position.y + height));
                positions.add(new Vector2D(position.x + width * 0.8f, position.y + height));
                break;
        }

        return positions;
    }


    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0;
        }

        // Flash when damaged
        state = BossState.DAMAGED;
        phaseChangeTime = System.currentTimeMillis();

        // Check if defeated
        if (health <= 0) {
            state = BossState.DEFEATED;
        }
    }

    @Override
    public void render(GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(position.x, position.y, 0);

        // Flash white when damaged
        if (state == BossState.DAMAGED && (System.currentTimeMillis() / 50) % 2 == 0) {
            gl.glColor3f(1.0f, 1.0f, 1.0f);
        }

        // Draw boss body
        drawBossBody(gl);

        // Draw health bar
        drawBossHealthBar(gl);

        gl.glPopMatrix();
    }


    private void drawBossBody(GL gl) {

        if (state != BossState.DAMAGED || (System.currentTimeMillis() / 50) % 2 == 1) {
            gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
        }

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(20, 0);
        gl.glVertex2f(width - 20, 0);
        gl.glVertex2f(width - 20, height);
        gl.glVertex2f(20, height);
        gl.glEnd();


        gl.glColor3f(0.7f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_TRIANGLES);

        gl.glVertex2f(0, 40);
        gl.glVertex2f(20, 20);
        gl.glVertex2f(20, 60);

        gl.glVertex2f(width, 40);
        gl.glVertex2f(width - 20, 20);
        gl.glVertex2f(width - 20, 60);
        gl.glEnd();


        gl.glColor3f(0.1f, 0.1f, 0.2f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(width * 0.3f, height * 0.2f);
        gl.glVertex2f(width * 0.7f, height * 0.2f);
        gl.glVertex2f(width * 0.7f, height * 0.5f);
        gl.glVertex2f(width * 0.3f, height * 0.5f);
        gl.glEnd();

        gl.glColor3f(1.0f, 1.0f, 0.0f);
        gl.glPointSize(8.0f);
        gl.glBegin(GL.GL_POINTS);
        gl.glVertex2f(width * 0.35f, height * 0.3f);
        gl.glVertex2f(width * 0.65f, height * 0.3f);
        gl.glEnd();

        drawCannons(gl);

        drawEngines(gl);

        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glLineWidth(3.0f);
        gl.glBegin(GL.GL_LINES);
        // Vertical lines
        gl.glVertex2f(width * 0.33f, 0);
        gl.glVertex2f(width * 0.33f, height);
        gl.glVertex2f(width * 0.66f, 0);
        gl.glVertex2f(width * 0.66f, height);
        gl.glVertex2f(20, height * 0.5f);
        gl.glVertex2f(width - 20, height * 0.5f);
        gl.glEnd();
    }

    private void drawCannons(GL gl) {
        gl.glColor3f(0.2f, 0.2f, 0.2f);

        switch (phase) {
            case 1:
                gl.glBegin(GL.GL_QUADS);
                gl.glVertex2f(width * 0.45f, height * 0.7f);
                gl.glVertex2f(width * 0.55f, height * 0.7f);
                gl.glVertex2f(width * 0.55f, height + 10);
                gl.glVertex2f(width * 0.45f, height + 10);
                gl.glEnd();
                break;

            case 2:
                gl.glBegin(GL.GL_QUADS);
                gl.glVertex2f(width * 0.25f, height * 0.7f);
                gl.glVertex2f(width * 0.3f, height * 0.7f);
                gl.glVertex2f(width * 0.3f, height + 10);
                gl.glVertex2f(width * 0.25f, height + 10);
                gl.glVertex2f(width * 0.7f, height * 0.7f);
                gl.glVertex2f(width * 0.75f, height * 0.7f);
                gl.glVertex2f(width * 0.75f, height + 10);
                gl.glVertex2f(width * 0.7f, height + 10);
                gl.glEnd();
                break;

            case 3:
                gl.glBegin(GL.GL_QUADS);
                gl.glVertex2f(width * 0.2f, height * 0.7f);
                gl.glVertex2f(width * 0.25f, height * 0.7f);
                gl.glVertex2f(width * 0.25f, height + 10);
                gl.glVertex2f(width * 0.2f, height + 10);
                gl.glVertex2f(width * 0.475f, height * 0.7f);
                gl.glVertex2f(width * 0.525f, height * 0.7f);
                gl.glVertex2f(width * 0.525f, height + 10);
                gl.glVertex2f(width * 0.475f, height + 10);
                gl.glVertex2f(width * 0.75f, height * 0.7f);
                gl.glVertex2f(width * 0.8f, height * 0.7f);
                gl.glVertex2f(width * 0.8f, height + 10);
                gl.glVertex2f(width * 0.75f, height + 10);
                gl.glEnd();
                break;
        }
    }

    private void drawEngines(GL gl) {
        float pulse = (float) Math.sin(System.currentTimeMillis() / 100.0) * 0.3f + 0.7f;
        gl.glColor3f(1.0f * pulse, 0.5f * pulse, 0.0f);

        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(width * 0.25f, 0);
        gl.glVertex2f(width * 0.2f, -15);
        gl.glVertex2f(width * 0.3f, -15);
        gl.glEnd();

        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(width * 0.75f, 0);
        gl.glVertex2f(width * 0.7f, -15);
        gl.glVertex2f(width * 0.8f, -15);
        gl.glEnd();
    }

    private void drawBossHealthBar(GL gl) {
        float barWidth = 400; // Large health bar
        float barHeight = 20;
        float barX = -position.x + 200; // Center on screen
        float barY = -position.y + 20; // Top of screen

        // Background (dark red)
        gl.glColor3f(0.3f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // Health (red to yellow based on health)
        float healthPercent = (float) health / maxHealth;
        float healthBarWidth = barWidth * healthPercent;

        // Color changes based on health
        float red = 1.0f;
        float green = healthPercent > 0.5f ? 0.0f : (0.5f - healthPercent) * 2.0f;
        gl.glColor3f(red, green, 0.0f);

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + healthBarWidth, barY);
        gl.glVertex2f(barX + healthBarWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // Border
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // Phase indicators (segments)
        gl.glColor3f(0.5f, 0.5f, 0.5f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINES);
        // 33% mark
        float segment1 = barX + barWidth * 0.33f;
        gl.glVertex2f(segment1, barY);
        gl.glVertex2f(segment1, barY + barHeight);
        // 66% mark
        float segment2 = barX + barWidth * 0.66f;
        gl.glVertex2f(segment2, barY);
        gl.glVertex2f(segment2, barY + barHeight);
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

    public int getPhase() {
        return phase;
    }

    public BossState getState() {
        return state;
    }

    public boolean isDefeated() {
        return state == BossState.DEFEATED;
    }

    public boolean isActive() {
        return state == BossState.ATTACKING;
    }
}