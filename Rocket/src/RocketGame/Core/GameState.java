package RocketGame.Core;

import RocketGame.Entities.*;
import RocketGame.Effects.Particle;
import RocketGame.Util.Constants;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    // Game status
    private boolean running;
    private boolean paused;
    private boolean gameOver;

    // Score & progression
    private int score;
    private int highScore;
    private int level;
    private int lives;
    private int combo;
    private long comboTimer;

    // Difficulty
    private float difficulty;
    private long lastObstacleSpawn;
    private long lastEnemySpawn;

    // Game objects
    private Rocket rocket;
    private List<Bullet> bullets;
    private List<Enemy> enemies;
    private List<Obstacle> obstacles;
    private List<PowerUp> powerups;
    private List<Particle> particles;
    private Boss boss;

    // Upgrades
    private float damageMultiplier;
    private float speedMultiplier;
    private float fireRateMultiplier;

    // Temporary powerup timers
    private long rapidFireEndTime;
    private long spreadShotEndTime;
    private long laserEndTime;
    private String previousWeapon;

    // Constructor
    public GameState() {
        initialize();
    }

    // Initialize all game state
    public void initialize() {
        running = true;
        paused = false;
        gameOver = false;

        score = 0;
        level = 1;
        lives = Constants.INITIAL_LIVES;
        combo = 0;
        comboTimer = System.currentTimeMillis();
        difficulty = 1.0f;

        // Create rocket at bottom center
        float rocketX = (Constants.WINDOW_WIDTH - Constants.ROCKET_WIDTH) / 2;
        float rocketY = Constants.WINDOW_HEIGHT - 100;
        rocket = new Rocket(rocketX, rocketY);

        // Initialize lists
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        obstacles = new ArrayList<>();
        powerups = new ArrayList<>();
        particles = new ArrayList<>();
        boss = null;

        // Upgrades
        damageMultiplier = 1.0f;
        speedMultiplier = 1.0f;
        fireRateMultiplier = 1.0f;

        // Spawn timers
        lastObstacleSpawn = System.currentTimeMillis();
        lastEnemySpawn = System.currentTimeMillis();

        // Powerup timers
        rapidFireEndTime = 0;
        spreadShotEndTime = 0;
        laserEndTime = 0;
        previousWeapon = "normal";

        // Load high score
        loadHighScore();
    }

    // Reset game (restart)
    public void reset() {
        initialize();
    }

    // Load high score from storage (or just keep in memory)
    private void loadHighScore() {
        // TODO: Load from file or preferences
        // For now, keep in memory
        if (highScore == 0) {
            highScore = 0;
        }
    }

    // Save high score
    private void saveHighScore() {
        // TODO: Save to file or preferences
        // For now, just update variable
    }

    // Update high score if current score is higher
    public void updateHighScore() {
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
    }

    // Update temporary powerups
    public void updateTemporaryPowerups() {
        long currentTime = System.currentTimeMillis();

        // Check rapid fire expiration
        if (rapidFireEndTime > 0 && currentTime >= rapidFireEndTime) {
            rocket.setFireRate(250); // Reset to normal
            rapidFireEndTime = 0;
        }

        // Check spread shot expiration
        if (spreadShotEndTime > 0 && currentTime >= spreadShotEndTime) {
            rocket.setWeaponType(previousWeapon);
            spreadShotEndTime = 0;
        }

        // Check laser expiration
        if (laserEndTime > 0 && currentTime >= laserEndTime) {
            rocket.setWeaponType(previousWeapon);
            laserEndTime = 0;
        }
    }

    // Activate temporary powerup
    public void activatePowerup(PowerUp.PowerupType type, int duration) {
        long currentTime = System.currentTimeMillis();

        switch (type) {
            case RAPID_FIRE:
                rapidFireEndTime = currentTime + duration;
                break;
            case SPREAD:
                if (!rocket.getWeaponType().equals("spread")) {
                    previousWeapon = rocket.getWeaponType();
                }
                spreadShotEndTime = currentTime + duration;
                break;
            case LASER:
                if (!rocket.getWeaponType().equals("laser")) {
                    previousWeapon = rocket.getWeaponType();
                }
                laserEndTime = currentTime + duration;
                break;
        }
    }

    // Level up
    public void levelUp() {
        level++;
        difficulty = 1.0f + (level - 1) * 0.3f;

        // Check if boss level (every 5 levels)
        if (level % Constants.BOSS_SPAWN_LEVEL == 0) {
            spawnBoss();
        }
    }

    // Spawn boss
    public void spawnBoss() {
        float bossX = (Constants.WINDOW_WIDTH - 160) / 2; // Center boss
        float bossY = -150; // Start above screen
        boss = new Boss(bossX, bossY);
    }

    // Game status getters/setters
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void togglePause() {
        paused = !paused;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        if (gameOver) {
            running = false;
            updateHighScore();
        }
    }

    // Score methods
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int points) {
        // Apply combo multiplier
        int bonusPoints = points;
        if (combo > 1) {
            bonusPoints = points * combo;
        }
        this.score += bonusPoints;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    // Level methods
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        this.difficulty = 1.0f + (level - 1) * 0.3f;
    }

    public float getDifficulty() {
        return difficulty;
    }

    // Lives methods
    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void addLife() {
        lives++;
    }

    public void loseLife() {
        lives--;
        if (lives <= 0) {
            lives = 0;
            setGameOver(true);
        }
    }

    // Combo methods
    public int getCombo() {
        return combo;
    }

    public void setCombo(int combo) {
        this.combo = combo;
    }

    public void incrementCombo() {
        combo++;
        comboTimer = System.currentTimeMillis();
    }

    public void resetCombo() {
        combo = 0;
    }

    public long getComboTimer() {
        return comboTimer;
    }

    public void setComboTimer(long comboTimer) {
        this.comboTimer = comboTimer;
    }

    // Entity getters
    public Rocket getRocket() {
        return rocket;
    }

    public void setRocket(Rocket rocket) {
        this.rocket = rocket;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public void setBullets(List<Bullet> bullets) {
        this.bullets = bullets;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public void setObstacles(List<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public List<PowerUp> getPowerups() {
        return powerups;
    }

    public void setPowerups(List<PowerUp> powerups) {
        this.powerups = powerups;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public void setParticles(List<Particle> particles) {
        this.particles = particles;
    }

    public Boss getBoss() {
        return boss;
    }

    public void setBoss(Boss boss) {
        this.boss = boss;
    }

    // Spawn timing
    public long getLastObstacleSpawn() {
        return lastObstacleSpawn;
    }

    public void setLastObstacleSpawn(long lastObstacleSpawn) {
        this.lastObstacleSpawn = lastObstacleSpawn;
    }

    public long getLastEnemySpawn() {
        return lastEnemySpawn;
    }

    public void setLastEnemySpawn(long lastEnemySpawn) {
        this.lastEnemySpawn = lastEnemySpawn;
    }

    // Upgrade methods
    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(float damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public void upgradeDamage() {
        damageMultiplier *= 1.5f;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public void upgradeSpeed() {
        speedMultiplier *= 1.2f;
    }

    public float getFireRateMultiplier() {
        return fireRateMultiplier;
    }

    public void setFireRateMultiplier(float fireRateMultiplier) {
        this.fireRateMultiplier = fireRateMultiplier;
    }

    public void upgradeFireRate() {
        fireRateMultiplier *= 1.5f;
    }

    // Utility methods
    public int getTotalEnemies() {
        int count = enemies.size();
        if (boss != null) {
            count++;
        }
        return count;
    }

    public int getTotalObjects() {
        return bullets.size() + enemies.size() + obstacles.size() +
                powerups.size() + particles.size();
    }

    public boolean hasBoss() {
        return boss != null;
    }

    public boolean isBossLevel() {
        return level % Constants.BOSS_SPAWN_LEVEL == 0;
    }

    // Clear all entities (for level transition)
    public void clearAllEntities() {
        bullets.clear();
        enemies.clear();
        obstacles.clear();
        powerups.clear();
        particles.clear();
        boss = null;
    }

    // Clear only enemies (keep bullets and powerups)
    public void clearEnemies() {
        enemies.clear();
        boss = null;
    }

    // Debug info
    @Override
    public String toString() {
        return "GameState{" +
                "running=" + running +
                ", paused=" + paused +
                ", gameOver=" + gameOver +
                ", score=" + score +
                ", level=" + level +
                ", lives=" + lives +
                ", combo=" + combo +
                ", difficulty=" + difficulty +
                ", enemies=" + enemies.size() +
                ", obstacles=" + obstacles.size() +
                ", bullets=" + bullets.size() +
                ", powerups=" + powerups.size() +
                ", hasBoss=" + hasBoss() +
                '}';
    }
}