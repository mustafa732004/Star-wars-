package RocketGame.Core;

import RocketGame.Entities.*;
import RocketGame.Effects.ParticleSystem;
import RocketGame.Input.InputHandler;
import RocketGame.Audio.SoundManager;
import RocketGame.Util.*;

import java.awt.event.KeyEvent;

public class GameEngine {

    private GameState gameState;
    private CollisionManager collisionManager;
    private ParticleSystem particleSystem;
    private InputHandler inputHandler;
    private SoundManager soundManager;


    private long lastUpdateTime;
    private float deltaTime;

    private long lastObstacleSpawn;
    private long lastEnemySpawn;
    private int obstacleSpawnInterval;  // Will be set dynamically
    private int enemySpawnInterval;

    private static final int MIN_OBSTACLE_INTERVAL = 500;   // At least 0.5 seconds
    private static final int MAX_OBSTACLE_INTERVAL = 2000;  // At most 2 seconds

    private boolean levelTransitioning;
    private long levelTransitionStartTime;
    private int levelTransitionDuration;

    private boolean showUpgradeMenu;
    private String[] upgradeOptions;
    private int selectedUpgrade;

    public GameEngine(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        this.soundManager = SoundManager.getInstance();
        this.particleSystem = new ParticleSystem();
        this.gameState = new GameState();
        this.collisionManager = new CollisionManager(particleSystem);

        this.lastUpdateTime = System.currentTimeMillis();
        this.deltaTime = 0;

        // Start with moderate spawn rates
        this.obstacleSpawnInterval = 1000; // 1 second initially
        this.enemySpawnInterval = 2000;    // 2 seconds initially
        this.lastObstacleSpawn = System.currentTimeMillis();
        this.lastEnemySpawn = System.currentTimeMillis();

        this.levelTransitioning = false;
        this.levelTransitionDuration = 2000;

        this.showUpgradeMenu = false;
        this.upgradeOptions = new String[]{"Extra Life", "Damage Boost", "Speed Boost", "Fire Rate"};
        this.selectedUpgrade = 0;
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        deltaTime = (currentTime - lastUpdateTime) / 1000.0f; // Convert to seconds
        lastUpdateTime = currentTime;

        if (deltaTime > 0.1f) {
            deltaTime = 0.1f;
        }

        if (gameState.isGameOver()) {
            handleGameOver();
            return;
        }

        if (gameState.isPaused()) {
            handlePauseInput();
            return;
        }

        if (showUpgradeMenu) {
            handleUpgradeMenuInput();
            return;
        }

        if (levelTransitioning) {
            handleLevelTransition();
            return;
        }

        handleInput();
        updateEntities();
        spawnEntities();
        checkCollisions();
        updateGameLogic();
        cleanupEntities();
    }


    private void handlePauseInput() {
        if (inputHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            gameState.togglePause();
        }
    }

    private void handleGameOver() {
        // Check for restart
        if (inputHandler.isKeyPressed(KeyEvent.VK_SPACE) ||
                inputHandler.isKeyPressed(KeyEvent.VK_ENTER)) {
            restart();
        }
    }

    private void shootBullet() {
        Rocket rocket = gameState.getRocket();

        if (!rocket.canShoot()) {
            return;
        }

        rocket.shoot();
        soundManager.playShoot();

        float bulletX = rocket.getX() + rocket.getWidth() / 2 - 3;
        float bulletY = rocket.getY();
        int damage = (int) (rocket.getDamage() * gameState.getDamageMultiplier());

        String weaponType = rocket.getWeaponType();

        if (weaponType.equals("spread")) {
            for (int i = -1; i <= 1; i++) {
                float angle = i * 15; // -15°, 0°, 15°
                Bullet bullet = new Bullet(bulletX, bulletY, damage, angle);
                gameState.getBullets().add(bullet);
            }
        } else if (weaponType.equals("laser")) {
            Bullet bullet = new Bullet(bulletX, bulletY, damage * 2, "laser");
            gameState.getBullets().add(bullet);
        } else {
            Bullet bullet = new Bullet(bulletX, bulletY, damage, "normal");
            gameState.getBullets().add(bullet);
        }
    }


    private void updateEntities() {
        gameState.getRocket().update(1.0f);

        for (int i = 0; i < gameState.getBullets().size(); i++) {
            gameState.getBullets().get(i).update(1.0f);
        }

        for (int i = 0; i < gameState.getEnemies().size(); i++) {
            Enemy enemy = gameState.getEnemies().get(i);
            enemy.update(1.0f);

            if (enemy.canShoot()) {
                float bulletX = enemy.getX() + enemy.getWidth() / 2;
                float bulletY = enemy.getY() + enemy.getHeight();
                Bullet enemyBullet = new Bullet(bulletX, bulletY, 10, true);
                gameState.getBullets().add(enemyBullet);
            }
        }

        for (int i = 0; i < gameState.getObstacles().size(); i++) {
            gameState.getObstacles().get(i).update(1.0f);
        }

        for (int i = 0; i < gameState.getPowerups().size(); i++) {
            gameState.getPowerups().get(i).update(1.0f);
        }

        if (gameState.getBoss() != null) {
            Boss boss = gameState.getBoss();
            boss.update(1.0f);

            if (boss.canShoot()) {
                java.util.List<Vector2D> positions = boss.getBulletSpawnPositions();
                for (int i = 0; i < positions.size(); i++) {
                    Vector2D pos = positions.get(i);
                    Bullet bossBullet = new Bullet(pos.x, pos.y, 15, true);
                    gameState.getBullets().add(bossBullet);
                }
            }
        }

        particleSystem.update(1.0f);

        gameState.updateTemporaryPowerups();

        if (Math.random() < 0.3) { // 30% chance each frame
            Rocket rocket = gameState.getRocket();
            particleSystem.createEngineTrail(
                    rocket.getX() + rocket.getWidth() / 2 - 2,
                    rocket.getY() + rocket.getHeight()
            );
        }
    }

    private void handleInput() {
        Rocket rocket = gameState.getRocket();

        float moveSpeed = rocket.getSpeed() * gameState.getSpeedMultiplier();

        if (inputHandler.isUpPressed()) {
            rocket.move(0, -moveSpeed, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        }
        if (inputHandler.isDownPressed()) {
            rocket.move(0, moveSpeed, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        }
        if (inputHandler.isLeftPressed()) {
            rocket.move(-moveSpeed, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        }
        if (inputHandler.isRightPressed()) {
            rocket.move(moveSpeed, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        }

        if (inputHandler.isSpacePressed()) {
            shootBullet();
        }

        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
            gameState.togglePause();
        }

        if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_M)) {
            soundManager.toggleMute();
        }
    }

    private void spawnEntities() {
        long currentTime = System.currentTimeMillis();

        if (gameState.hasBoss()) {
            return;
        }

        float difficulty = gameState.getDifficulty();
        int adjustedObstacleInterval = (int) (obstacleSpawnInterval / difficulty);
        int adjustedEnemyInterval = (int) (enemySpawnInterval / difficulty);

        adjustedObstacleInterval = Math.max(MIN_OBSTACLE_INTERVAL, adjustedObstacleInterval);
        adjustedEnemyInterval = Math.max(1500, adjustedEnemyInterval);

        if (currentTime - lastObstacleSpawn > adjustedObstacleInterval) {
            int baseCount = 3;
            int extraCount = (int) difficulty;
            int obstacleCount = baseCount + extraCount;


            obstacleCount = Math.min(obstacleCount, 8);

            for (int i = 0; i < obstacleCount; i++) {
                spawnObstacle();
            }

            System.out.println("Spawned " + obstacleCount + " obstacles (difficulty: " + difficulty + ")");
            lastObstacleSpawn = currentTime;
        }


        if (currentTime - lastEnemySpawn > adjustedEnemyInterval) {
            spawnEnemy();
            lastEnemySpawn = currentTime;
        }

        if (Math.random() < 0.002) {
            spawnPowerup();
        }
    }


    private void spawnObstacle() {
        float width = 40 + (float) (Math.random() * 40);
        float height = 40 + (float) (Math.random() * 20);
        float x = (float) (Math.random() * (Constants.WINDOW_WIDTH - width));
        float y = -height;


        float speed = Constants.ENEMY_BASE_SPEED * gameState.getDifficulty();

        Obstacle obstacle = new Obstacle(x, y, width, height, speed);
        gameState.getObstacles().add(obstacle);

        System.out.println("Spawned obstacle at (" + x + ", " + y + ") with speed " + speed);
    }


    private void spawnEnemy() {
        float x = (float) (Math.random() * (Constants.WINDOW_WIDTH - 40));
        float y = -50;
        float speed = Constants.ENEMY_BASE_SPEED * gameState.getDifficulty();


        Enemy.EnemyType[] types = Enemy.EnemyType.values();
        Enemy.EnemyType type = types[(int) (Math.random() * types.length)];

        Enemy enemy = new Enemy(x, y, speed, type);
        gameState.getEnemies().add(enemy);
    }


    private void spawnPowerup() {
        float x = (float) (Math.random() * (Constants.WINDOW_WIDTH - 30));
        float y = -30;

        PowerUp powerup = new PowerUp(x, y);
        gameState.getPowerups().add(powerup);
    }


    private void checkCollisions() {
        collisionManager.checkAllCollisions(gameState);
        collisionManager.updateCombo(gameState);
    }


    private void updateGameLogic() {
        // Check if all enemies cleared and no boss
        if (gameState.getEnemies().isEmpty() &&
                gameState.getObstacles().isEmpty() &&
                !gameState.hasBoss() &&
                gameState.getScore() > 0) {


            checkLevelUp();
        }
    }


    private void checkLevelUp() {
        // Level up based on score milestones
        int scorePerLevel = 1000;
        int expectedLevel = (gameState.getScore() / scorePerLevel) + 1;

        if (expectedLevel > gameState.getLevel()) {
            startLevelTransition();
        }
    }


    private void startLevelTransition() {
        levelTransitioning = true;
        levelTransitionStartTime = System.currentTimeMillis();
        gameState.clearAllEntities();


        showUpgradeMenu = true;
        selectedUpgrade = 0;
    }


    private void handleLevelTransition() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - levelTransitionStartTime;

        if (elapsed > levelTransitionDuration) {
            levelTransitioning = false;
            gameState.levelUp();
        }
    }

    private void handleUpgradeMenuInput() {
        if (inputHandler.isKeyPressed(KeyEvent.VK_UP) || inputHandler.isKeyPressed(KeyEvent.VK_W)) {
            selectedUpgrade--;
            if (selectedUpgrade < 0) {
                selectedUpgrade = upgradeOptions.length - 1;
            }
        }

        if (inputHandler.isKeyPressed(KeyEvent.VK_DOWN) || inputHandler.isKeyPressed(KeyEvent.VK_S)) {
            selectedUpgrade++;
            if (selectedUpgrade >= upgradeOptions.length) {
                selectedUpgrade = 0;
            }
        }

        if (inputHandler.isKeyPressed(KeyEvent.VK_SPACE) ||
                inputHandler.isKeyPressed(KeyEvent.VK_ENTER)) {
            applyUpgrade(selectedUpgrade);
            showUpgradeMenu = false;
        }
    }

    public void applyUpgrade(int upgradeIndex) {
        switch (upgradeIndex) {
            case 0: // Extra Life
                gameState.addLife();
                break;
            case 1: // Damage Boost
                gameState.upgradeDamage();
                break;
            case 2: // Speed Boost
                gameState.upgradeSpeed();
                break;
            case 3: // Fire Rate
                gameState.upgradeFireRate();
                break;
        }
    }

    private void cleanupEntities() {
        for (int i = gameState.getBullets().size() - 1; i >= 0; i--) {
            Bullet bullet = gameState.getBullets().get(i);
            if (bullet.isOffScreen(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT)) {
                gameState.getBullets().remove(i);
            }
        }

        for (int i = gameState.getEnemies().size() - 1; i >= 0; i--) {
            Enemy enemy = gameState.getEnemies().get(i);
            if (enemy.isOutOfBounds(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT)) {
                gameState.getEnemies().remove(i);
            }
        }

        for (int i = gameState.getObstacles().size() - 1; i >= 0; i--) {
            Obstacle obstacle = gameState.getObstacles().get(i);
            if (obstacle.isOutOfBounds(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT)) {
                gameState.getObstacles().remove(i);
                gameState.addScore(Constants.SCORE_OBSTACLE_DODGE);
            }
        }

        for (int i = gameState.getPowerups().size() - 1; i >= 0; i--) {
            PowerUp powerup = gameState.getPowerups().get(i);
            if (powerup.isOutOfBounds(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT)) {
                gameState.getPowerups().remove(i);
            }
        }
    }


    public void restart() {
        gameState.reset();
        particleSystem.clear();
        lastObstacleSpawn = System.currentTimeMillis();
        lastEnemySpawn = System.currentTimeMillis();
        levelTransitioning = false;
        showUpgradeMenu = false;
    }


    public GameState getGameState() {
        return gameState;
    }

    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    public boolean isShowingUpgradeMenu() {
        return showUpgradeMenu;
    }

    public String[] getUpgradeOptions() {
        return upgradeOptions;
    }

    public int getSelectedUpgrade() {
        return selectedUpgrade;
    }

    public boolean isLevelTransitioning() {
        return levelTransitioning;
    }

    public float getDeltaTime() {
        return deltaTime;
    }
}