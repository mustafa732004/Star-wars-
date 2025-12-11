package RocketGame.Entities;

import javax.media.opengl.GL;
import RocketGame.Util.Vector2D;

public class PowerUp extends GameObject {
    private PowerupType type;
    private float speed;
    private float rotation;
    private float pulse;
    private long spawnTime;

    // Powerup types
    public enum PowerupType {
        HEALTH,
        SHIELD,
        RAPID_FIRE,
        SPREAD,
        LASER,
        COIN,
        EXTRA_LIFE
    }


    public PowerUp(float x, float y, PowerupType type) {
        super(x, y, 30, 30);

        this.type = type;
        this.speed = 2.0f;
        this.velocity.set(0, speed);
        this.rotation = 0;
        this.pulse = 1.0f;
        this.spawnTime = System.currentTimeMillis();
    }

    public PowerUp(float x, float y) {
        this(x, y, getRandomType());
    }

    private static PowerupType getRandomType() {
        PowerupType[] types = PowerupType.values();
        return types[(int) (Math.random() * types.length)];
    }

    @Override
    public void update(float deltaTime) {
        position.y += velocity.y * deltaTime;

        rotation += 3.0f * deltaTime;
        if (rotation >= 360) {
            rotation -= 360;
        }

        long time = System.currentTimeMillis() - spawnTime;
        pulse = 1.0f + (float) Math.sin(time / 200.0) * 0.2f;
    }

    @Override
    public void render(GL gl) {
        gl.glPushMatrix();

        gl.glDisable(GL.GL_TEXTURE_2D);

        gl.glTranslatef(position.x + width / 2, position.y + height / 2, 0);

        gl.glRotatef(rotation, 0, 0, 1);

        gl.glScalef(pulse, pulse, 1.0f);

        switch (type) {
            case HEALTH:
                drawHealth(gl);
                break;
            case SHIELD:
                drawShield(gl);
                break;
            case RAPID_FIRE:
                drawRapidFire(gl);
                break;
            case SPREAD:
                drawSpread(gl);
                break;
            case LASER:
                drawLaser(gl);
                break;
            case COIN:
                drawCoin(gl);
                break;
            case EXTRA_LIFE:
                drawExtraLife(gl);
                break;
        }

        gl.glEnable(GL.GL_TEXTURE_2D);

        gl.glPopMatrix();
    }

    private void drawHealth(GL gl) {

        gl.glColor3f(0.9f, 0.2f, 0.2f);
        drawSquare(gl, 15);


        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(5.0f);
        gl.glBegin(GL.GL_LINES);
        gl.glVertex2f(-10, 0);
        gl.glVertex2f(10, 0);
        gl.glVertex2f(0, -10);
        gl.glVertex2f(0, 10);
        gl.glEnd();


        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(2.0f);
        drawSquareOutline(gl, 15);
    }

    private void drawShield(GL gl) {
        gl.glColor3f(0.2f, 0.6f, 1.0f);
        drawCircle(gl, 15);


        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(4.0f);
        drawHexagon(gl, 10);


        gl.glLineWidth(2.0f);
        drawHexagon(gl, 6);
    }

    private void drawRapidFire(GL gl) {

        gl.glColor3f(1.0f, 0.9f, 0.2f);
        drawSquare(gl, 15);


        gl.glColor3f(1.0f, 0.5f, 0.0f);

        drawRect(gl, -9, -8, 3, 16);
        drawRect(gl, -1.5f, -8, 3, 16);
        drawRect(gl, 6, -8, 3, 16);


        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(2.0f);
        drawSquareOutline(gl, 15);
    }

    private void drawSpread(GL gl) {

        gl.glColor3f(0.2f, 0.9f, 0.3f);
        drawSquare(gl, 15);

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(3.0f);

        gl.glBegin(GL.GL_LINES);
        gl.glVertex2f(0, 8);
        gl.glVertex2f(-8, -8);

        gl.glVertex2f(0, 8);
        gl.glVertex2f(0, -10);

        gl.glVertex2f(0, 8);
        gl.glVertex2f(8, -8);
        gl.glEnd();


        drawArrowTip(gl, -8, -8, 225);
        drawArrowTip(gl, 0, -10, 270);
        drawArrowTip(gl, 8, -8, 315);
    }

    private void drawLaser(GL gl) {

        gl.glColor3f(0.0f, 0.9f, 0.9f);
        drawSquare(gl, 15);


        gl.glColor3f(1.0f, 1.0f, 1.0f);
        drawRect(gl, -2, -12, 4, 24);


        gl.glColor3f(0.8f, 1.0f, 1.0f);
        drawRect(gl, -5, -12, 2, 24);
        drawRect(gl, 3, -12, 2, 24);


        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(2.0f);
        drawSquareOutline(gl, 15);
    }

    private void drawCoin(GL gl) {

        gl.glColor3f(1.0f, 0.84f, 0.0f);
        drawCircle(gl, 15);


        gl.glColor3f(0.85f, 0.65f, 0.0f);
        drawCircle(gl, 12);

        gl.glColor3f(1.0f, 1.0f, 0.6f);
        gl.glLineWidth(3.0f);
        gl.glBegin(GL.GL_LINES);

        gl.glVertex2f(-6, -6);
        gl.glVertex2f(6, 6);
        gl.glVertex2f(-6, 6);
        gl.glVertex2f(6, -6);
        gl.glEnd();


        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glPointSize(4.0f);
        gl.glBegin(GL.GL_POINTS);
        gl.glVertex2f(-5, 5);
        gl.glEnd();
    }

    private void drawExtraLife(GL gl) {
        gl.glColor3f(1.0f, 0.2f, 0.4f);

        gl.glBegin(GL.GL_TRIANGLE_FAN);
        gl.glVertex2f(0, -8);
        gl.glVertex2f(-8, 0);
        gl.glVertex2f(-8, 3);
        gl.glVertex2f(-6, 5);
        gl.glVertex2f(-4, 5);
        gl.glVertex2f(-2, 4);
        gl.glVertex2f(0, 3);
        gl.glVertex2f(2, 4);
        gl.glVertex2f(4, 5);
        gl.glVertex2f(6, 5);
        gl.glVertex2f(8, 3);
        gl.glVertex2f(8, 0);
        gl.glEnd();

        gl.glColor3f(1.0f, 0.8f, 0.9f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(-4, 2);
        gl.glVertex2f(-2, 3);
        gl.glVertex2f(-3, 0);
        gl.glEnd();
    }

    private void drawSquare(GL gl, float size) {
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-size, -size);
        gl.glVertex2f(size, -size);
        gl.glVertex2f(size, size);
        gl.glVertex2f(-size, size);
        gl.glEnd();
    }

    private void drawSquareOutline(GL gl, float size) {
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(-size, -size);
        gl.glVertex2f(size, -size);
        gl.glVertex2f(size, size);
        gl.glVertex2f(-size, size);
        gl.glEnd();
    }

    private void drawCircle(GL gl, float radius) {
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        gl.glVertex2f(0, 0);
        for (int i = 0; i <= 360; i += 15) {
            double angle = Math.toRadians(i);
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;
            gl.glVertex2f(x, y);
        }
        gl.glEnd();
    }

    private void drawHexagon(GL gl, float radius) {
        gl.glBegin(GL.GL_LINE_LOOP);
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60);
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;
            gl.glVertex2f(x, y);
        }
        gl.glEnd();
    }

    private void drawRect(GL gl, float x, float y, float w, float h) {
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + w, y);
        gl.glVertex2f(x + w, y + h);
        gl.glVertex2f(x, y + h);
        gl.glEnd();
    }

    private void drawArrowTip(GL gl, float x, float y, float angle) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, 0);
        gl.glRotatef(angle, 0, 0, 1);

        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(-3, -5);
        gl.glVertex2f(3, -5);
        gl.glEnd();

        gl.glPopMatrix();
    }

    public void applyToRocket(Rocket rocket) {
        switch (type) {
            case HEALTH:
                rocket.heal(30);
                break;
            case SHIELD:
                rocket.addShield(50);
                break;
            case RAPID_FIRE:
                rocket.setFireRate(100);
                break;
            case SPREAD:
                rocket.setWeaponType("spread");
                break;
            case LASER:
                rocket.setWeaponType("laser");
                break;
        }
    }

    public int getScoreValue() {
        return type == PowerupType.COIN ? 100 : 0;
    }

    public boolean givesExtraLife() {
        return type == PowerupType.EXTRA_LIFE;
    }

    public int getDuration() {
        switch (type) {
            case RAPID_FIRE:
                return 5000;
            case SPREAD:
            case LASER:
                return 8000;
            default:
                return 0;
        }
    }

    public boolean isTemporary() {
        return type == PowerupType.RAPID_FIRE ||
                type == PowerupType.SPREAD ||
                type == PowerupType.LASER;
    }

    public String getName() {
        switch (type) {
            case HEALTH:
                return "Health Pack";
            case SHIELD:
                return "Shield Boost";
            case RAPID_FIRE:
                return "Rapid Fire";
            case SPREAD:
                return "Spread Shot";
            case LASER:
                return "Laser Beam";
            case COIN:
                return "Coin";
            case EXTRA_LIFE:
                return "Extra Life";
            default:
                return "Powerup";
        }
    }

    public PowerupType getType() {
        return type;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        this.velocity.set(0, speed);
    }
}