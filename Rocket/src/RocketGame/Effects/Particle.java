package RocketGame.Effects;

import javax.media.opengl.GL;
import RocketGame.Util.Vector2D;

public class Particle {
    private Vector2D position;
    private Vector2D velocity;
    private float[] color; // rgb
    private float size;
    private float life;
    private float decay;
    private ParticleType type;


    public enum ParticleType {
        EXPLOSION,
        SPARK,
        SMOKE,
        DEBRIS,
        STAR
    }

    public Particle(float x, float y, float vx, float vy, float[] color, float size, ParticleType type) {
        this.position = new Vector2D(x, y);
        this.velocity = new Vector2D(vx, vy);
        this.color = color;
        this.size = size;
        this.life = 1.0f;
        this.type = type;

        switch (type) {
            case EXPLOSION:
                this.decay = 0.02f;
                break;
            case SPARK:
                this.decay = 0.03f;
                break;
            case SMOKE:
                this.decay = 0.015f;
                break;
            case DEBRIS:
                this.decay = 0.025f;
                break;
            case STAR:
                this.decay = 0.01f;
                break;
        }
    }

    public Particle(float x, float y, float vx, float vy, float[] color, float size) {
        this(x, y, vx, vy, color, size, ParticleType.EXPLOSION);
    }

    public void update(float deltaTime) {
        position.x += velocity.x * deltaTime;
        position.y += velocity.y * deltaTime;

        if (type == ParticleType.DEBRIS || type == ParticleType.SMOKE) {
            velocity.y += 0.2f * deltaTime;
        }

        velocity.x *= 0.98f;
        velocity.y *= 0.98f;

        life -= decay;

        if (type == ParticleType.SMOKE) {
            size += 0.1f;
        } else {
            size *= 0.97f;
        }
    }

    public void render(GL gl) {
        if (life <= 0) return;

        gl.glPushMatrix();
        gl.glTranslatef(position.x, position.y, 0);

        float fadeFactor = life;
        gl.glColor3f(color[0] * fadeFactor, color[1] * fadeFactor, color[2] * fadeFactor);

        switch (type) {
            case EXPLOSION:
                drawExplosionParticle(gl);
                break;
            case SPARK:
                drawSparkParticle(gl);
                break;
            case SMOKE:
                drawSmokeParticle(gl);
                break;
            case DEBRIS:
                drawDebrisParticle(gl);
                break;
            case STAR:
                drawStarParticle(gl);
                break;
        }

        gl.glPopMatrix();
    }

    private void drawExplosionParticle(GL gl) {
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        gl.glVertex2f(0, 0);
        for (int i = 0; i <= 360; i += 45) {
            double angle = Math.toRadians(i);
            float x = (float) Math.cos(angle) * size;
            float y = (float) Math.sin(angle) * size;
            gl.glVertex2f(x, y);
        }
        gl.glEnd();
    }

    private void drawSparkParticle(GL gl) {
        gl.glLineWidth(size);
        gl.glBegin(GL.GL_LINES);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(-velocity.x * 0.5f, -velocity.y * 0.5f);
        gl.glEnd();
    }

    private void drawSmokeParticle(GL gl) {
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        gl.glVertex2f(0, 0);
        for (int i = 0; i <= 360; i += 30) {
            double angle = Math.toRadians(i);
            float x = (float) Math.cos(angle) * size;
            float y = (float) Math.sin(angle) * size;
            gl.glVertex2f(x, y);
        }
        gl.glEnd();
    }

    private void drawDebrisParticle(GL gl) {
        float halfSize = size / 2;
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-halfSize, -halfSize);
        gl.glVertex2f(halfSize, -halfSize);
        gl.glVertex2f(halfSize, halfSize);
        gl.glVertex2f(-halfSize, halfSize);
        gl.glEnd();
    }

    private void drawStarParticle(GL gl) {
        gl.glPointSize(size);
        gl.glBegin(GL.GL_POINTS);
        gl.glVertex2f(0, 0);
        gl.glEnd();

        if (life > 0.5f) {
            gl.glLineWidth(1.0f);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(-size, 0);
            gl.glVertex2f(size, 0);
            gl.glVertex2f(0, -size);
            gl.glVertex2f(0, size);
            gl.glEnd();
        }
    }

    public boolean isDead() {
        return life <= 0;
    }

    public Vector2D getPosition() {
        return position;
    }

    public float getLife() {
        return life;
    }

    public ParticleType getType() {
        return type;
    }
}