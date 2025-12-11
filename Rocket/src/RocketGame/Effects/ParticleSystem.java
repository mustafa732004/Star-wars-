package RocketGame.Effects;

import javax.media.opengl.GL;
import java.util.ArrayList;
import java.util.List;

public class ParticleSystem {
    private final List<Particle> particles;

    public ParticleSystem() {
        this.particles = new ArrayList<>();
    }

    public void update(float deltaTime) {

        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).update(deltaTime);
        }


        for (int i = particles.size() - 1; i >= 0; i--) {
            if (particles.get(i).isDead()) {
                particles.remove(i);
            }
        }
    }

    public void render(GL gl) {
        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).render(gl);
        }
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public void createExplosion(float x, float y, float[] color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            float speed = (float) (Math.random() * 5 + 2);
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;
            float size = (float) (Math.random() * 3 + 2);

            Particle particle = new Particle(x, y, vx, vy, color, size, Particle.ParticleType.EXPLOSION);
            particles.add(particle);
        }
    }

    public void createSparks(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            float speed = (float) (Math.random() * 8 + 4);
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;

            float[] color = new float[]{
                    1.0f,
                    (float) (Math.random() * 0.3 + 0.7),
                    (float) (Math.random() * 0.3)
            };

            float size = (float) (Math.random() * 2 + 1);

            Particle particle = new Particle(x, y, vx, vy, color, size, Particle.ParticleType.SPARK);
            particles.add(particle);
        }
    }

    public void createSmoke(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            float vx = (float) (Math.random() - 0.5) * 2;
            float vy = (float) (Math.random() - 0.5) * 2;

            float gray = (float) (Math.random() * 0.3 + 0.4);
            float[] color = new float[]{gray, gray, gray};

            float size = (float) (Math.random() * 5 + 3);

            Particle particle = new Particle(x, y, vx, vy, color, size, Particle.ParticleType.SMOKE);
            particles.add(particle);
        }
    }

    public void createDebris(float x, float y, float[] color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            float speed = (float) (Math.random() * 4 + 1);
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed - 2;

            float size = (float) (Math.random() * 4 + 2);

            Particle particle = new Particle(x, y, vx, vy, color, size, Particle.ParticleType.DEBRIS);
            particles.add(particle);
        }
    }

    public void createHitEffect(float x, float y, float[] color) {
        createExplosion(x, y, color, 8);
        createSparks(x, y, 4);
    }

    public void createLargeExplosion(float x, float y, float[] color) {
        createExplosion(x, y, color, 30);
        createSparks(x, y, 15);
        createSmoke(x, y, 10);
        createDebris(x, y, color, 12);
    }

    public void createBossExplosion(float x, float y) {
        float[][] colors = {
                {1.0f, 0.0f, 0.0f},
                {1.0f, 0.5f, 0.0f},
                {1.0f, 1.0f, 0.0f},
                {1.0f, 1.0f, 1.0f}
        };

        for (int i = 0; i < colors.length; i++) {
            createExplosion(x, y, colors[i], 40);
        }

        createSparks(x, y, 30);
        createSmoke(x, y, 20);
    }

    public void createEngineTrail(float x, float y) {
        float[] color = new float[]{
                1.0f,
                (float) (Math.random() * 0.3 + 0.5),
                0.0f
        };

        float vx = (float) (Math.random() - 0.5) * 1;
        float vy = (float) (Math.random() * 2 + 1);
        float size = (float) (Math.random() * 3 + 2);

        Particle particle = new Particle(x, y, vx, vy, color, size, Particle.ParticleType.EXPLOSION);
        particles.add(particle);
    }

    public void createStarField(int screenWidth, int screenHeight, int count) {
        for (int i = 0; i < count; i++) {
            float x = (float) (Math.random() * screenWidth);
            float y = (float) (Math.random() * screenHeight);
            float vy = (float) (Math.random() * 2 + 1);

            float brightness = (float) (Math.random() * 0.5 + 0.5);
            float[] color = new float[]{brightness, brightness, brightness};

            float size = (float) (Math.random() * 2 + 1);

            Particle particle = new Particle(x, y, 0, vy, color, size, Particle.ParticleType.STAR);
            particles.add(particle);
        }
    }

    public void createPowerupEffect(float x, float y, float[] color) {
        for (int i = 0; i < 20; i++) {
            double angle = (Math.PI * 2 / 20) * i;
            float speed = 3.0f;
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;

            float size = (float) (Math.random() * 3 + 2);

            Particle particle = new Particle(x, y, vx, vy, color, size, Particle.ParticleType.SPARK);
            particles.add(particle);
        }
    }

    public void clear() {
        particles.clear();
    }

    public int getParticleCount() {
        return particles.size();
    }

    public boolean isEmpty() {
        return particles.isEmpty();
    }
}