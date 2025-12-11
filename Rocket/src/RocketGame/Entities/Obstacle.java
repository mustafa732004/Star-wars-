package RocketGame.Entities;


import javax.media.opengl.GL;

public class Obstacle extends GameObject {
    private int health;
    private int maxHealth;
    private float[] color;
    private ObstacleType type;

    public enum ObstacleType {
        ASTEROID,
        METEOR,
        DEBRIS
    }


    public Obstacle(float x, float y, float width, float height, float speed) {
        super(x, y, width, height);

        this.health = 1;
        this.maxHealth = 1;


        this.velocity.set(0, speed);  // Move down the screen


        int random = (int) (Math.random() * 3);
        if (random == 0) {
            this.type = ObstacleType.ASTEROID;
            this.color = new float[]{0.6f, 0.6f, 0.6f};
        } else if (random == 1) {
            this.type = ObstacleType.METEOR;
            this.color = new float[]{1.0f, 0.5f, 0.2f};
        } else {
            this.type = ObstacleType.DEBRIS;
            this.color = new float[]{0.7f, 0.3f, 0.7f};
        }
    }

    @Override
    public void update(float deltaTime) {

        position.x += velocity.x;
        position.y += velocity.y;
    }



    @Override
    public void render(GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(position.x, position.y, 0);

        switch (type) {
            case ASTEROID:
                drawAsteroid(gl);
                break;
            case METEOR:
                drawMeteor(gl);
                break;
            case DEBRIS:
                drawDebris(gl);
                break;
        }

        if (health < maxHealth) {
            drawHealthBar(gl);
        }

        gl.glPopMatrix();
    }

    private void drawAsteroid(GL gl) {
        gl.glColor3f(color[0], color[1], color[2]);

        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2f(width * 0.2f, 0);
        gl.glVertex2f(width * 0.8f, 0);
        gl.glVertex2f(width, height * 0.3f);
        gl.glVertex2f(width * 0.9f, height);
        gl.glVertex2f(width * 0.1f, height);
        gl.glVertex2f(0, height * 0.4f);
        gl.glEnd();

        gl.glColor3f(color[0] * 0.5f, color[1] * 0.5f, color[2] * 0.5f);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2f(width * 0.3f, height * 0.3f);
        gl.glVertex2f(width * 0.4f, height * 0.3f);
        gl.glVertex2f(width * 0.4f, height * 0.5f);
        gl.glVertex2f(width * 0.3f, height * 0.5f);
        gl.glEnd();

        gl.glColor3f(color[0] * 1.3f, color[1] * 1.3f, color[2] * 1.3f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(width * 0.2f, 0);
        gl.glVertex2f(width * 0.8f, 0);
        gl.glVertex2f(width, height * 0.3f);
        gl.glVertex2f(width * 0.9f, height);
        gl.glVertex2f(width * 0.1f, height);
        gl.glVertex2f(0, height * 0.4f);
        gl.glEnd();
    }

    private void drawMeteor(GL gl) {
        gl.glColor3f(color[0], color[1], color[2]);

        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2f(width * 0.5f, 0);
        gl.glVertex2f(width, height * 0.4f);
        gl.glVertex2f(width * 0.7f, height);
        gl.glVertex2f(width * 0.3f, height);
        gl.glVertex2f(0, height * 0.4f);
        gl.glEnd();

        gl.glColor3f(1.0f, 0.9f, 0.3f);
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        float cx = width * 0.5f;
        float cy = height * 0.5f;
        float radius = Math.min(width, height) * 0.2f;
        gl.glVertex2f(cx, cy);
        for (int i = 0; i <= 360; i += 30) {
            double radians = Math.toRadians(i);
            float x = cx + (float) Math.cos(radians) * radius;
            float y = cy + (float) Math.sin(radians) * radius;
            gl.glVertex2f(x, y);
        }
        gl.glEnd();

        gl.glColor3f(1.0f, 0.7f, 0.0f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(width * 0.5f, 0);
        gl.glVertex2f(width, height * 0.4f);
        gl.glVertex2f(width * 0.7f, height);
        gl.glVertex2f(width * 0.3f, height);
        gl.glVertex2f(0, height * 0.4f);
        gl.glEnd();
    }

    private void drawDebris(GL gl) {
        gl.glColor3f(color[0], color[1], color[2]);

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(width, 0);
        gl.glVertex2f(width, height);
        gl.glVertex2f(0, height);
        gl.glEnd();

        gl.glColor3f(color[0] * 0.6f, color[1] * 0.6f, color[2] * 0.6f);
        gl.glLineWidth(1.5f);
        gl.glBegin(GL.GL_LINES);
        gl.glVertex2f(width * 0.5f, 0);
        gl.glVertex2f(width * 0.5f, height);
        gl.glVertex2f(0, height * 0.5f);
        gl.glVertex2f(width, height * 0.5f);
        gl.glEnd();

        gl.glColor3f(1.0f, 0.0f, 1.0f);
        gl.glPointSize(3.0f);
        gl.glBegin(GL.GL_POINTS);
        gl.glVertex2f(width * 0.25f, height * 0.25f);
        gl.glVertex2f(width * 0.75f, height * 0.75f);
        gl.glEnd();

        gl.glColor3f(color[0] * 1.3f, color[1] * 1.3f, color[2] * 1.3f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(width, 0);
        gl.glVertex2f(width, height);
        gl.glVertex2f(0, height);
        gl.glEnd();
    }

    private void drawHealthBar(GL gl) {
        float barWidth = width;
        float barHeight = 4;
        float barY = -8;

        gl.glColor3f(0.8f, 0.1f, 0.1f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, barY);
        gl.glVertex2f(barWidth, barY);
        gl.glVertex2f(barWidth, barY + barHeight);
        gl.glVertex2f(0, barY + barHeight);
        gl.glEnd();

        float healthWidth = barWidth * ((float) health / maxHealth);
        gl.glColor3f(0.1f, 0.8f, 0.1f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, barY);
        gl.glVertex2f(healthWidth, barY);
        gl.glVertex2f(healthWidth, barY + barHeight);
        gl.glVertex2f(0, barY + barHeight);
        gl.glEnd();

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(1.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(0, barY);
        gl.glVertex2f(barWidth, barY);
        gl.glVertex2f(barWidth, barY + barHeight);
        gl.glVertex2f(0, barY + barHeight);
        gl.glEnd();
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0;
        }
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
        this.maxHealth = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public ObstacleType getType() {
        return type;
    }

    public float[] getColor() {
        return color;
    }
}