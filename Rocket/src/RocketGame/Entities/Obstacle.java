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

    private static HashMap<String, Texture> planetTextures = new HashMap<>();
    private static boolean texturesLoaded = false;
    private Texture currentTexture;

    // إضافة متغير لحفظ الصورة المختارة لكل عقبة
    private String selectedPlanetName;

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
        // CRITICAL: Actually move the obstacle!
        position.x += velocity.x;
        position.y += velocity.y;
    }

    // تحميل صور الكواكب
    private static void loadPlanetTextures() {
        try {
            // تحميل 7 صور للكواكب
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

    // اختيار صورة كوكب عشوائية
    private void selectPlanetTexture() {
        if (planetTextures.isEmpty()) {
            currentTexture = null;
            selectedPlanetName = null;
            return;
        }

        // اختيار صورة عشوائية من بين الصور المحملة
        String[] keys = planetTextures.keySet().toArray(new String[0]);
        int randomIndex = (int) (Math.random() * keys.length);
        selectedPlanetName = keys[randomIndex];
        currentTexture = planetTextures.get(selectedPlanetName);

        // تعيين لون بناءً على نوع الكوكب المختار
        adjustColorByPlanet(selectedPlanetName);
    }

    // تعديل اللون بناءً على صورة الكوكب المختارة
    private void adjustColorByPlanet(String planetName) {
        if (planetName == null) return;

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
        // حفظ حالة المصفوفة الحالية
        gl.glPushMatrix();

        // تطبيق التحويلات لهذه العقبة فقط
        gl.glTranslatef(position.x, position.y, 0);

        // تفعيل خاصية الخلط للشفافية
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // استخدام الصورة إذا كانت متوفرة
        if (currentTexture != null && texturesLoaded) {
            drawPlanetWithTexture(gl);
        } else {
            // رسم الشكل القديم كنسخة احتياطية
            drawBackupShape(gl);
        }

        // تعطيل الخلط بعد الانتهاء
        gl.glDisable(GL.GL_BLEND);

        // رسم شريط الصحة إذا لزم الأمر
        if (health < maxHealth) {
            drawHealthBar(gl);
        }

        // استعادة حالة المصفوفة السابقة
        gl.glPopMatrix();

        // تنظيف حالة النسيج للتأكد من عدم تداخل النسيج مع الكائنات الأخرى
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }

    // رسم الكوكب باستخدام الصورة (الإصدار المصحح)
    private void drawPlanetWithTexture(GL gl) {
        // تمكين النسيج
        gl.glEnable(GL.GL_TEXTURE_2D);

        // ربط النسيج الحالي
        currentTexture.bind();

        // تعيين وضع النسيج
        gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

        // استخدام اللون المحدد للكوكب
        gl.glColor3f(color[0], color[1], color[2]);

        // رسم الكوكب كدائرة باستخدام النسيج
        drawTexturedCircle(gl, width / 2, height / 2, Math.min(width, height) / 2);

        // تعطيل النسيج بعد الانتهاء
        gl.glDisable(GL.GL_TEXTURE_2D);
    }

    // رسم دائرة مع نسيج
    private void drawTexturedCircle(GL gl, float cx, float cy, float radius) {
        int segments = 32;
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        gl.glTexCoord2f(0.5f, 0.5f); // مركز النسيج
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

    // النسخة الاحتياطية إذا فشل تحميل الصور
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

    // الطرق القديمة للرسم (للنسخ الاحتياطي)
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

    // دالة HealthBar
    private void drawHealthBar(GL gl) {
        float barWidth = width;
        float barHeight = 4;
        float barY = -8;

        // الخلفية الحمراء
        gl.glColor3f(0.8f, 0.1f, 0.1f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, barY);
        gl.glVertex2f(barWidth, barY);
        gl.glVertex2f(barWidth, barY + barHeight);
        gl.glVertex2f(0, barY + barHeight);
        gl.glEnd();

        // الجزء الأخضر (الصحة الحالية)
        float healthWidth = barWidth * ((float) health / maxHealth);
        gl.glColor3f(0.1f, 0.8f, 0.1f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, barY);
        gl.glVertex2f(healthWidth, barY);
        gl.glVertex2f(healthWidth, barY + barHeight);
        gl.glVertex2f(0, barY + barHeight);
        gl.glEnd();

        // الحدود البيضاء
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

    public String getSelectedPlanetName() {
        return selectedPlanetName;
    }

    // دالة إضافية قد تكون مفيدة
    public boolean isOutOfBounds(int screenWidth, int screenHeight) {
        return position.y > screenHeight ||
                position.x < -width ||
                position.x > screenWidth;
    }
}