package RocketGame.Entities;

import javax.media.opengl.GL;

import RocketGame.Entities.GameObject;
import RocketGame.Util.Constants;
import RocketGame.Util.Vector2D;

public class Bullet extends GameObject {
    private int damage;
    private float speed;
    private boolean fromEnemy;
    private String bulletType;
    private float[] color;

    public Bullet(float x, float y, int damage, String bulletType) {
        super(x, y, Constants.BULLET_WIDTH, Constants.BULLET_HEIGHT);

        this.damage = damage;
        this.speed = Constants.BULLET_SPEED;
        this.fromEnemy = false;
        this.bulletType = bulletType;

        if (bulletType.equals("laser")) {
            this.color = new float[]{0.0f, 1.0f, 1.0f}; // Cyan
            this.width = 4;
            this.height = 30;
        } else {
            this.color = new float[]{0.0f, 1.0f, 0.0f}; // Green
        }

        velocity.set(0, -speed);
    }

    public Bullet(float x, float y, int damage, boolean isEnemyBullet) {
        super(x, y, Constants.BULLET_WIDTH, Constants.BULLET_HEIGHT);

        this.damage = damage;
        this.speed = Constants.BULLET_SPEED * 0.8f;
        this.fromEnemy = isEnemyBullet;
        this.bulletType = "normal";

        if (isEnemyBullet) {
            this.color = new float[]{1.0f, 0.0f, 0.0f};
                        velocity.set(0, speed);
        } else {
            this.color = new float[]{0.0f, 1.0f, 0.0f};
            velocity.set(0, -speed);
        }
    }


    public Bullet(float x, float y, int damage, float angle) {
        super(x, y, Constants.BULLET_WIDTH, Constants.BULLET_HEIGHT);

        this.damage = damage;
        this.speed = Constants.BULLET_SPEED;
        this.fromEnemy = false;
        this.bulletType = "spread";
        this.color = new float[]{0.0f, 1.0f, 0.0f}; // Green


        float radians = (float) Math.toRadians(angle);
        velocity.set((float) Math.sin(radians) * speed,
                -(float) Math.cos(radians) * speed);
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


        gl.glColor3f(color[0], color[1], color[2]);

        if (bulletType.equals("laser")) {

            drawLaserBullet(gl);
        } else {

            drawNormalBullet(gl);
        }

        gl.glPopMatrix();
    }


    private void drawNormalBullet(GL gl) {

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(width, 0);
        gl.glVertex2f(width, height);
        gl.glVertex2f(0, height);
        gl.glEnd();


        gl.glLineWidth(1.5f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(width, 0);
        gl.glVertex2f(width, height);
        gl.glVertex2f(0, height);
        gl.glEnd();
    }


    private void drawLaserBullet(GL gl) {

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(width, 0);
        gl.glVertex2f(width, height);
        gl.glVertex2f(0, height);
        gl.glEnd();


        gl.glColor3f(1.0f, 1.0f, 1.0f); // White center
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(width * 0.25f, 0);
        gl.glVertex2f(width * 0.75f, 0);
        gl.glVertex2f(width * 0.75f, height);
        gl.glVertex2f(width * 0.25f, height);
        gl.glEnd();
    }


    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return position.x + width < 0 ||
                position.x > screenWidth ||
                position.y + height < 0 ||
                position.y > screenHeight;
    }

        public int getDamage() {
        return damage;
    }

    public boolean isFromEnemy() {
        return fromEnemy;
    }

    public String getBulletType() {
        return bulletType;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}