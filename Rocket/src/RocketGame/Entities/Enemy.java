package RocketGame.Entities;

import javax.media.opengl.GL;
import RocketGame.Util.Vector2D;

public class Enemy extends GameObject {
    private int health;
    private int maxHealth;
    private float speed;
    private EnemyType type;
    private long lastShot;
    private int shootInterval; // milliseconds between shots
    private float direction; // -1 = left, 1 = right
    private float[] color;

    // Enemy types
    public enum EnemyType {
        BASIC,      // Just moves down
        ZIGZAG,     // Moves side to side
        SHOOTER,    // Stays and shoots
        KAMIKAZE    // Rushes at player
    }

    // Constructor
    public Enemy(float x, float y, float speed, EnemyType type) {
        super(x, y, 40, 40); // 40x40 default size

        this.speed = speed;
        this.type = type;
        this.lastShot = System.currentTimeMillis();
        this.direction = Math.random() < 0.5 ? -1 : 1;

        // Set properties based on type
        switch (type) {
            case BASIC:
                this.health = 1;
                this.maxHealth = 1;
                this.color = new float[]{0.7f, 0.3f, 0.7f}; // Purple
                this.velocity.set(0, speed);
                break;

            case ZIGZAG:
                this.health = 2;
                this.maxHealth = 2;
                this.color = new float[]{0.3f, 0.7f, 0.7f}; // Cyan
                this.velocity.set(direction * 2, speed * 0.7f);
                break;

            case SHOOTER:
                this.health = 3;
                this.maxHealth = 3;
                this.color = new float[]{1.0f, 0.5f, 0.3f}; // Orange
                this.velocity.set(0, speed * 0.5f);
                this.shootInterval = 2000; // Shoot every 2 seconds
                break;

            case KAMIKAZE:
                this.health = 1;
                this.maxHealth = 1;
                this.color = new float[]{1.0f, 0.2f, 0.2f}; // Red
                this.velocity.set(0, speed * 1.5f);
                break;
        }
    }

    @Override
    public void update(float deltaTime) {
        // Update position
        position.x += velocity.x * deltaTime;
        position.y += velocity.y * deltaTime;

        // Type-specific behavior
        switch (type) {
            case ZIGZAG:
                updateZigzagMovement();
                break;
            case KAMIKAZE:
                // Accelerate downward
                velocity.y += 0.1f;
                break;
        }
    }

    // Zigzag movement pattern
    private void updateZigzagMovement() {
        // Change direction at screen edges
        if (position.x <= 0) {
            direction = 1;
            velocity.x = direction * 2;
        } else if (position.x >= 800 - width) { // Assuming 800 width screen
            direction = -1;
            velocity.x = direction * 2;
        }
    }

    // Check if enemy can shoot
    public boolean canShoot() {
        if (type != EnemyType.SHOOTER) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now - lastShot >= shootInterval) {
            lastShot = now;
            return true;
        }
        return false;
    }

    @Override
    public void render(GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(position.x, position.y, 0);

        // Draw based on type
        switch (type) {
            case BASIC:
                drawBasicEnemy(gl);
                break;
            case ZIGZAG:
                drawZigzagEnemy(gl);
                break;
            case SHOOTER:
                drawShooterEnemy(gl);
                break;
            case KAMIKAZE:
                drawKamikazeEnemy(gl);
                break;
        }

        // Draw health bar if damaged
        if (health < maxHealth) {
            drawHealthBar(gl);
        }

        gl.glPopMatrix();
    }

    // Draw basic enemy (triangle)
    private void drawBasicEnemy(GL gl) {
        gl.glColor3f(color[0], color[1], color[2]);

        // Triangle pointing down
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(width * 0.5f, height); // Bottom point
        gl.glVertex2f(0, 0);                  // Top left
        gl.glVertex2f(width, 0);              // Top right
        gl.glEnd();

        // Eyes
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glPointSize(4.0f);
        gl.glBegin(GL.GL_POINTS);
        gl.glVertex2f(width * 0.3f, height * 0.3f);
        gl.glVertex2f(width * 0.7f, height * 0.3f);
        gl.glEnd();

        // Outline
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(width * 0.5f, height);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(width, 0);
        gl.glEnd();
    }

    // Draw zigzag enemy (diamond)
    private void drawZigzagEnemy(GL gl) {
        gl.glColor3f(color[0], color[1], color[2]);

        // Diamond shape
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(width * 0.5f, 0);        // Top
        gl.glVertex2f(width, height * 0.5f);   // Right
        gl.glVertex2f(width * 0.5f, height);   // Bottom
        gl.glVertex2f(0, height * 0.5f);       // Left
        gl.glEnd();

        // Center decoration
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(width * 0.4f, height * 0.4f);
        gl.glVertex2f(width * 0.6f, height * 0.4f);
        gl.glVertex2f(width * 0.6f, height * 0.6f);
        gl.glVertex2f(width * 0.4f, height * 0.6f);
        gl.glEnd();

        // Outline
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(width * 0.5f, 0);
        gl.glVertex2f(width, height * 0.5f);
        gl.glVertex2f(width * 0.5f, height);
        gl.glVertex2f(0, height * 0.5f);
        gl.glEnd();
    }

    // Draw shooter enemy (hexagon with turret)
    private void drawShooterEnemy(GL gl) {
        gl.glColor3f(color[0], color[1], color[2]);

        // Hexagon body
        gl.glBegin(GL.GL_POLYGON);
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60);
            float x = width * 0.5f + (float) Math.cos(angle) * width * 0.5f;
            float y = height * 0.5f + (float) Math.sin(angle) * height * 0.5f;
            gl.glVertex2f(x, y);
        }
        gl.glEnd();

        // Turret (gun)
        gl.glColor3f(0.2f, 0.2f, 0.2f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(width * 0.4f, height * 0.6f);
        gl.glVertex2f(width * 0.6f, height * 0.6f);
        gl.glVertex2f(width * 0.6f, height);
        gl.glVertex2f(width * 0.4f, height);
        gl.glEnd();

        // Warning light
        long blink = System.currentTimeMillis() / 300;
        if (blink % 2 == 0) {
            gl.glColor3f(1.0f, 0.0f, 0.0f);
            gl.glPointSize(6.0f);
            gl.glBegin(GL.GL_POINTS);
            gl.glVertex2f(width * 0.5f, height * 0.4f);
            gl.glEnd();
        }
    }

    // Draw kamikaze enemy (skull-like)
    private void drawKamikazeEnemy(GL gl) {
        gl.glColor3f(color[0], color[1], color[2]);

        // Main body (circle)
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        gl.glVertex2f(width * 0.5f, height * 0.5f);
        for (int i = 0; i <= 360; i += 20) {
            double radians = Math.toRadians(i);
            float x = width * 0.5f + (float) Math.cos(radians) * width * 0.4f;
            float y = height * 0.5f + (float) Math.sin(radians) * height * 0.4f;
            gl.glVertex2f(x, y);
        }
        gl.glEnd();

        // Angry eyes (X shape)
        gl.glColor3f(0.0f, 0.0f, 0.0f);
        gl.glLineWidth(3.0f);
        // Left eye X
        gl.glBegin(GL.GL_LINES);
        gl.glVertex2f(width * 0.25f, height * 0.35f);
        gl.glVertex2f(width * 0.35f, height * 0.45f);
        gl.glVertex2f(width * 0.35f, height * 0.35f);
        gl.glVertex2f(width * 0.25f, height * 0.45f);
        // Right eye X
        gl.glVertex2f(width * 0.65f, height * 0.35f);
        gl.glVertex2f(width * 0.75f, height * 0.45f);
        gl.glVertex2f(width * 0.75f, height * 0.35f);
        gl.glVertex2f(width * 0.65f, height * 0.45f);
        gl.glEnd();

        // Mouth (angry)
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex2f(width * 0.3f, height * 0.6f);
        gl.glVertex2f(width * 0.5f, height * 0.7f);
        gl.glVertex2f(width * 0.7f, height * 0.6f);
        gl.glEnd();
    }

    // Draw health bar
    private void drawHealthBar(GL gl) {
        float barWidth = width;
        float barHeight = 4;
        float barY = -8;

        // Background (red)
        gl.glColor3f(0.8f, 0.1f, 0.1f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, barY);
        gl.glVertex2f(barWidth, barY);
        gl.glVertex2f(barWidth, barY + barHeight);
        gl.glVertex2f(0, barY + barHeight);
        gl.glEnd();

        // Health (green)
        float healthWidth = barWidth * ((float) health / maxHealth);
        gl.glColor3f(0.1f, 0.8f, 0.1f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, barY);
        gl.glVertex2f(healthWidth, barY);
        gl.glVertex2f(healthWidth, barY + barHeight);
        gl.glVertex2f(0, barY + barHeight);
        gl.glEnd();
    }

    // Take damage
    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0;
        }
    }

    // Check if destroyed
    public boolean isDestroyed() {
        return health <= 0;
    }

    // Getters
    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public EnemyType getType() {
        return type;
    }

    public float getSpeed() {
        return speed;
    }
}