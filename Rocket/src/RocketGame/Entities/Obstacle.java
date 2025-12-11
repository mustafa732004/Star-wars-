package RocketGame.Entities;

import javax.media.opengl.GL;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Obstacle extends GameObject {
    private int health;
    private int maxHealth;
    private float[] color;
    private ObstacleType type;
    private float rotation;

    private static HashMap<String, Texture> planetTextures = new HashMap<>();
    private static boolean texturesLoaded = false;
    private Texture currentTexture;

    public enum ObstacleType {
        ASTEROID,
        METEOR,
        DEBRIS
    }

    // Constructor
    public Obstacle(float x, float y, float width, float height, float speed) {
        super(x, y, width, height);

        this.health = 1;
        this.maxHealth = 1;

        // Set velocity to move downward
        this.velocity.set(0, speed);

        // Random obstacle type
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

        // تحميل الصور إذا لم تكن محملة
        if (!texturesLoaded) {
            loadPlanetTextures();
        }

        // اختيار صورة عشوائية للكوكب
        selectPlanetTexture();
    }

    @Override
    public void update(float deltaTime) {
        position.x += velocity.x;
        position.y += velocity.y;
    }

    private static void loadPlanetTextures() {
        try {
            String[] planetFiles = {
                    "planet6.png",
                    "planet1.png",
                    "planet2.png",
                    "planet3.png",
                    "planet4.png",
                    "planet5.png",
                    "planet7.png",
            };

            for (String fileName : planetFiles) {
                String texturePath = "Assets/" + fileName;
                File textureFile = new File(texturePath);

                if (textureFile.exists()) {
                    Texture texture = TextureIO.newTexture(textureFile, true);
                    texture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
                    texture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
                    planetTextures.put(fileName, texture);
                    System.out.println("Loaded planet texture: " + fileName);
                } else {
                    System.err.println("Planet texture not found: " + texturePath);
                }
            }

            texturesLoaded = true;
            System.out.println("Total planet textures loaded: " + planetTextures.size());

        } catch (IOException e) {
            System.err.println("Error loading planet textures: " + e.getMessage());
        }
    }

    private void selectPlanetTexture() {
        if (planetTextures.isEmpty()) {
            currentTexture = null;
            return;
        }

        String[] keys = planetTextures.keySet().toArray(new String[0]);
        int randomIndex = (int) (Math.random() * keys.length);
        String selectedKey = keys[randomIndex];
        currentTexture = planetTextures.get(selectedKey);

        adjustColorByPlanet(selectedKey);
    }

    private void adjustColorByPlanet(String planetName) {
        switch (planetName) {
            case "planet6.png":
                this.color = new float[]{0.8f, 0.8f, 0.9f}; // أزرق فاتح
                break;
            case "planet1.png":
                this.color = new float[]{1.0f, 0.7f, 0.3f}; // برتقالي
                break;
            case "planet2.png":
                this.color = new float[]{0.6f, 0.9f, 0.6f}; // أخضر
                break;
            case "planet3.png":
                this.color = new float[]{0.9f, 0.6f, 0.6f}; // أحمر فاتح
                break;
            case "planet4.png":
                this.color = new float[]{0.8f, 0.6f, 0.9f}; // بنفسجي
                break;
            case "planet5.png":
                this.color = new float[]{0.9f, 0.9f, 0.6f}; // أصفر
                break;
            case "planet7.png":
                this.color = new float[]{0.9f, 0.9f, 0.6f}; // أصفر
                break;
            default:
                this.color = new float[]{0.7f, 0.7f, 0.7f}; // رمادي
        }
    }

    @Override
    public void render(GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(position.x, position.y, 0);

        if (currentTexture != null && texturesLoaded) {
            drawPlanetWithTexture(gl);
            gl.glTranslatef(position.x + width/2, position.y + height/2, 0);
            gl.glRotatef(rotation, 0, 0, 1);
            gl.glTranslatef(-width/2, -height/2, 0);
        } else {
            drawBackupShape(gl);
        }

        if (health < maxHealth) {
            drawHealthBar(gl);
        }

        gl.glPopMatrix();
    }

    private void drawPlanetWithTexture(GL gl) {
        currentTexture.bind();
        currentTexture.enable();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

        gl.glColor3f(color[0], color[1], color[2]);

        // رسم الكوكب كدائرة باستخدام النسيج
        drawTexturedCircle(gl, width / 2, height / 2, Math.min(width, height) / 2);

        currentTexture.disable();
        gl.glDisable(GL.GL_TEXTURE_2D);
    }

    private void drawTexturedCircle(GL gl, float cx, float cy, float radius) {
        int segments = 32;
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        gl.glTexCoord2f(0.5f, 0.5f);
        gl.glVertex2f(cx, cy);

        for (int i = 0; i <= segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            float tx = (float) (0.5 + 0.5 * Math.cos(angle));
            float ty = (float) (0.5 + 0.5 * Math.sin(angle));

            float x = cx + radius * (float) Math.cos(angle);
            float y = cy + radius * (float) Math.sin(angle);

            gl.glTexCoord2f(tx, ty);
            gl.glVertex2f(x, y);
        }
        gl.glEnd();
    }

    private void drawBackupShape(GL gl) {
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
    }

    private void drawDebris(GL gl) {
        gl.glColor3f(color[0], color[1], color[2]);

        gl.glBegin(GL.GL_QUADS);
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

    public boolean isOutOfBounds(int screenWidth, int screenHeight) {
        return position.y > screenHeight ||
                position.x < -width ||
                position.x > screenWidth;
    }
}