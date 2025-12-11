package RocketGame.Core;

import RocketGame.Entities.*;
import RocketGame.Effects.Particle;
import RocketGame.Util.Constants;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private boolean running;
    private boolean paused;
    private boolean gameOver;
    private boolean victory;
    private boolean twoPlayerMode;

    private int score;
    private int scorePlayer2;
    private int highScore;
    private int level;
    private int lives;
    private int livesPlayer2;
    private int combo;
    private long comboTimer;

    private float difficulty;
    private long lastObstacleSpawn;
    private long lastEnemySpawn;

    private Rocket rocket;
    private Rocket rocket2;
    private List<Bullet> bullets;
    private List<Enemy> enemies;
    private List<Obstacle> obstacles;
    private List<PowerUp> powerups;
    private List<Particle> particles;
    private Boss boss;

    private float damageMultiplier;
    private float speedMultiplier;
    private float fireRateMultiplier;

    private long rapidFireEndTime;
    private long spreadShotEndTime;
    private long laserEndTime;
    private String previousWeapon;

    private boolean multiplayer;

    public GameState() {
        initialize(false, false);
    }

    public void initialize(boolean isMultiplayer, boolean isTwoPlayer) {
        this.multiplayer = isMultiplayer;
        this.twoPlayerMode = isTwoPlayer;

        running = true;
        paused = false;
        gameOver = false;
        victory = false;

        score = 0;
        scorePlayer2 = 0;
        level = 1;
        lives = Constants.INITIAL_LIVES;
        livesPlayer2 = Constants.INITIAL_LIVES;
        combo = 0;
        comboTimer = System.currentTimeMillis();
        difficulty = 1.0f;

        float rocketX = (Constants.WINDOW_WIDTH - Constants.ROCKET_WIDTH) / 2;
        float rocketY = Constants.WINDOW_HEIGHT - 100;
        rocket = new Rocket(rocketX, rocketY);

        if (twoPlayerMode) {
            rocket2 = new Rocket(rocketX - 100, rocketY);
        } else {
            rocket2 = null;
        }

        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        obstacles = new ArrayList<>();
        powerups = new ArrayList<>();
        particles = new ArrayList<>();
        boss = null;

        damageMultiplier = 1.0f;
        speedMultiplier = 1.0f;
        fireRateMultiplier = 1.0f;

        lastObstacleSpawn = System.currentTimeMillis();
        lastEnemySpawn = System.currentTimeMillis();

        rapidFireEndTime = 0;
        spreadShotEndTime = 0;
        laserEndTime = 0;
        previousWeapon = "normal";

        loadHighScore();
    }

    public void reset() {
        initialize(multiplayer, twoPlayerMode);
    }

    private void loadHighScore() {
        if (highScore == 0) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
    }

    public void updateHighScore() {
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
    }

    public void updateTemporaryPowerups() {
        long currentTime = System.currentTimeMillis();

        if (rapidFireEndTime > 0 && currentTime >= rapidFireEndTime) {
            rocket.setFireRate(250);
            rapidFireEndTime = 0;
        }

        if (spreadShotEndTime > 0 && currentTime >= spreadShotEndTime) {
            rocket.setWeaponType(previousWeapon);
            spreadShotEndTime = 0;
        }

        if (laserEndTime > 0 && currentTime >= laserEndTime) {
            rocket.setWeaponType(previousWeapon);
            laserEndTime = 0;
        }
    }

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

    public void levelUp() {
        level++;
        difficulty = 1.0f + (level - 1) * 0.3f;

        if (level == Constants.BOSS_SPAWN_LEVEL) {
            spawnBoss();
        }
    }

    public void spawnBoss() {
        float bossX = (Constants.WINDOW_WIDTH - 200) / 2;
        float bossY = -200;
        boss = new Boss(bossX, bossY);
    }

    // Getters and Setters
    public boolean isTwoPlayerMode() { return twoPlayerMode; }
    public void setTwoPlayerMode(boolean twoPlayerMode) { this.twoPlayerMode = twoPlayerMode; }

    public int getScorePlayer2() { return scorePlayer2; }
    public void setScorePlayer2(int score) { this.scorePlayer2 = score; }
    public void addScorePlayer2(int points) {
        int bonusPoints = points;
        if (combo > 1) {
            bonusPoints = points * combo;
        }
        this.scorePlayer2 += bonusPoints;
    }

    public int getLivesPlayer2() { return livesPlayer2; }
    public void setLivesPlayer2(int lives) { this.livesPlayer2 = lives; }
    public void addLifePlayer2() { livesPlayer2++; }
    public void loseLifePlayer2() {
        livesPlayer2--;
        if (livesPlayer2 <= 0) {
            livesPlayer2 = 0;
            if (lives <= 0) {
                setGameOver(true);
            }
        }
    }

    public Rocket getRocket2() { return rocket2; }
    public void setRocket2(Rocket rocket2) { this.rocket2 = rocket2; }

    public boolean isVictory() { return victory; }
    public void setVictory(boolean victory) { this.victory = victory; }

    public boolean isRunning() { return running; }
    public void setRunning(boolean running) { this.running = running; }
    public boolean isPaused() { return paused; }
    public void setPaused(boolean paused) { this.paused = paused; }
    public void togglePause() { paused = !paused; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        if (gameOver) {
            running = false;
            updateHighScore();
        }
    }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public void addScore(int points) {
        int bonusPoints = points;
        if (combo > 1) {
            bonusPoints = points * combo;
        }
        this.score += bonusPoints;
    }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }
    public int getLevel() { return level; }
    public void setLevel(int level) {
        this.level = level;
        this.difficulty = 1.0f + (level - 1) * 0.3f;
    }
    public float getDifficulty() { return difficulty; }
    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }
    public void addLife() { lives++; }
    public void loseLife() {
        lives--;
        if (lives <= 0) {
            lives = 0;
            if (livesPlayer2 <= 0 || !twoPlayerMode) {
                setGameOver(true);
            }
        }
    }

    public int getCombo() { return combo; }
    public void setCombo(int combo) { this.combo = combo; }
    public void incrementCombo() {
        combo++;
        comboTimer = System.currentTimeMillis();
    }
    public void resetCombo() { combo = 0; }
    public long getComboTimer() { return comboTimer; }
    public void setComboTimer(long comboTimer) { this.comboTimer = comboTimer; }

    public Rocket getRocket() { return rocket; }
    public void setRocket(Rocket rocket) { this.rocket = rocket; }
    public List<Bullet> getBullets() { return bullets; }
    public void setBullets(List<Bullet> bullets) { this.bullets = bullets; }
    public List<Enemy> getEnemies() { return enemies; }
    public void setEnemies(List<Enemy> enemies) { this.enemies = enemies; }
    public List<Obstacle> getObstacles() { return obstacles; }
    public void setObstacles(List<Obstacle> obstacles) { this.obstacles = obstacles; }
    public List<PowerUp> getPowerups() { return powerups; }
    public void setPowerups(List<PowerUp> powerups) { this.powerups = powerups; }
    public List<Particle> getParticles() { return particles; }
    public void setParticles(List<Particle> particles) { this.particles = particles; }
    public Boss getBoss() { return boss; }
    public void setBoss(Boss boss) { this.boss = boss; }

    public long getLastObstacleSpawn() { return lastObstacleSpawn; }
    public void setLastObstacleSpawn(long lastObstacleSpawn) { this.lastObstacleSpawn = lastObstacleSpawn; }
    public long getLastEnemySpawn() { return lastEnemySpawn; }
    public void setLastEnemySpawn(long lastEnemySpawn) { this.lastEnemySpawn = lastEnemySpawn; }

    public float getDamageMultiplier() { return damageMultiplier; }
    public void setDamageMultiplier(float damageMultiplier) { this.damageMultiplier = damageMultiplier; }
    public void upgradeDamage() { damageMultiplier *= 1.5f; }
    public float getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(float speedMultiplier) { this.speedMultiplier = speedMultiplier; }
    public void upgradeSpeed() { speedMultiplier *= 1.2f; }
    public float getFireRateMultiplier() { return fireRateMultiplier; }
    public void setFireRateMultiplier(float fireRateMultiplier) { this.fireRateMultiplier = fireRateMultiplier; }
    public void upgradeFireRate() { fireRateMultiplier *= 1.5f; }

    public int getTotalEnemies() {
        int count = enemies.size();
        if (boss != null) {
            count++;
        }
        return count;
    }

    public int getTotalObjects() {
        return bullets.size() + enemies.size() + obstacles.size() + powerups.size() + particles.size();
    }

    public boolean hasBoss() { return boss != null; }
    public boolean isBossLevel() { return level % Constants.BOSS_SPAWN_LEVEL == 0; }

    public void clearAllEntities() {
        bullets.clear();
        enemies.clear();
        obstacles.clear();
        powerups.clear();
        particles.clear();
        boss = null;
    }

    public void clearEnemies() {
        enemies.clear();
        boss = null;
    }
}