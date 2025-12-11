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

    private long gameOverTimestamp = 0;

    private long lastUpdateTime;
    private float deltaTime;

    private long lastObstacleSpawn;
    private long lastEnemySpawn;
    private int obstacleSpawnInterval;
    private int enemySpawnInterval;

    private static final int MIN_OBSTACLE_INTERVAL = 500;
    private static final int MAX_OBSTACLE_INTERVAL = 2000;

    private boolean levelTransitioning;
    private long levelTransitionStartTime;
    private static final int LEVEL_TRANSITION_DURATION = 2000; // 2 seconds

    private boolean showUpgradeMenu;
    private String[] upgradeOptions;
    private int selectedUpgrade;

    private boolean levelThreeComplete = false;
    private int levelThreeMenuSelection = 0;

    private boolean mouseShooting = false;
    private long lastMouseShot = 0;
    private int mouseFireRate = 250;

    private long upgradeMenuStartTime;

    public GameEngine(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        this.soundManager = SoundManager.getInstance();
        this.particleSystem = new ParticleSystem();
        this.gameState = new GameState();
        this.collisionManager = new CollisionManager(particleSystem);

        this.lastUpdateTime = System.currentTimeMillis();
        this.deltaTime = 0;

        this.obstacleSpawnInterval = 1000;
        this.enemySpawnInterval = 2000;
        this.lastObstacleSpawn = System.currentTimeMillis();
        this.lastEnemySpawn = System.currentTimeMillis();

        this.levelTransitioning = false;

        this.showUpgradeMenu = false;
        this.upgradeOptions = new String[]{"Damage Boost", "Speed Boost", "Fire Rate"};
        this.selectedUpgrade = 0;
    }

    public void resetGame() {
        restart();
    }

    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        if (deltaTime > 0.1f) {
            deltaTime = 0.1f;
        }

        if (gameState.isGameOver() && levelThreeComplete) {
            handleLevelThreeMenu();
            return;
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
            handleLevelTransition(currentTime);
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
        if (inputHandler != null && inputHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            gameState.togglePause();
        }
    }

    private void handleGameOver() {
        if (gameOverTimestamp == 0) {
            gameOverTimestamp = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - gameOverTimestamp < 2000) {
            return;
        }

        if (inputHandler != null && (inputHandler.isKeyPressed(KeyEvent.VK_SPACE) ||
                inputHandler.isKeyPressed(KeyEvent.VK_ENTER))) {
            restart();
        }
    }

    private void handleLevelThreeMenu() {
        if (inputHandler == null) return;

        if (inputHandler.isMouseButtonPressed()) {
            int mouseX = inputHandler.getMouseX();
            int mouseY = inputHandler.getMouseY();

            if (mouseX >= Constants.WINDOW_WIDTH/2 - 100 && mouseX <= Constants.WINDOW_WIDTH/2 + 100) {
                if (mouseY >= 280 && mouseY <= 330) {
                    levelThreeMenuSelection = 0;
                    handleLevelThreeChoice();
                }
                else if (mouseY >= 350 && mouseY <= 400) {
                    levelThreeMenuSelection = 1;
                    handleLevelThreeChoice();
                }
                else if (mouseY >= 420 && mouseY <= 470) {
                    levelThreeMenuSelection = 2;
                    handleLevelThreeChoice();
                }
            }
        }
    }

    public void handleLevelThreeMenuSelection(int selection) {
        this.levelThreeMenuSelection = selection;

        handleLevelThreeChoice();
    }

    private void handleLevelThreeChoice() {
        switch (levelThreeMenuSelection) {
            case 0: // Play Again
                restart();
                levelThreeComplete = false;
                gameState.setGameOver(false);
                System.out.println("Restarting game from level 1...");
                break;

            case 1:
                levelThreeComplete = false;
                gameState.setGameOver(false);

                if (inputHandler != null && inputHandler.mainGame != null) {
                    System.out.println("Returning to Main Menu via GameEngine...");
                    inputHandler.mainGame.returnToMainMenu();
                } else {
                    System.out.println("ERROR: Cannot return to home - mainGame is null");
                    System.out.println("InputHandler: " + inputHandler);
                    System.out.println("mainGame: " + (inputHandler != null ? inputHandler.mainGame : "null"));
                }
                break;

            case 2: // Exit Game
                System.out.println("Exiting game...");
                System.exit(0);
                break;
        }
    }

    private void shootBullet(Rocket rocket, boolean isPlayer2) {
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
                float angle = i * 15;
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

    private void shootWithMouse() {
        // Player 2 shooting with mouse
        if (gameState.getRocket2() == null) return;

        long currentTime = System.currentTimeMillis();
        if (mouseShooting && currentTime - lastMouseShot >= mouseFireRate) {
            Rocket rocket2 = gameState.getRocket2();
            if (!rocket2.canShoot()) return;

            rocket2.shoot();
            soundManager.playShoot();

            float bulletX = rocket2.getX() + rocket2.getWidth() / 2 - 3;
            float bulletY = rocket2.getY();
            int damage = (int) (rocket2.getDamage() * gameState.getDamageMultiplier());

            Bullet bullet = new Bullet(bulletX, bulletY, damage, "normal");
            gameState.getBullets().add(bullet);

            lastMouseShot = currentTime;
        }
    }

    private void updateEntities() {
        gameState.getRocket().update(1.0f);

        if (gameState.getRocket2() != null) {
            gameState.getRocket2().update(1.0f);
        }

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
                    Bullet bossBullet = new Bullet(pos.x, pos.y, 15, true , "planet");
                    gameState.getBullets().add(bossBullet);
                }
            }
        }

        particleSystem.update(1.0f);
        gameState.updateTemporaryPowerups();

        // Create engine trails for both rockets
        if (Math.random() < 0.3) {
            Rocket rocket = gameState.getRocket();
            particleSystem.createEngineTrail(
                    rocket.getX() + rocket.getWidth() / 2 - 2,
                    rocket.getY() + rocket.getHeight()
            );
        }

        if (gameState.getRocket2() != null && Math.random() < 0.3) {
            Rocket rocket2 = gameState.getRocket2();
            particleSystem.createEngineTrail(
                    rocket2.getX() + rocket2.getWidth() / 2 - 2,
                    rocket2.getY() + rocket2.getHeight()
            );
        }
    }

    private void handleInput() {
        if (inputHandler == null) return;

        // Player 1 controls (Arrow Keys + Space)
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
            shootBullet(rocket, false);
        }

        // Player 2 controls (WASD + Mouse)
        if (gameState.getRocket2() != null) {
            Rocket rocket2 = gameState.getRocket2();

            // WASD movement
            if (inputHandler.isKeyPressed(KeyEvent.VK_W)) {
                rocket2.move(0, -moveSpeed, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            }
            if (inputHandler.isKeyPressed(KeyEvent.VK_S)) {
                rocket2.move(0, moveSpeed, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            }
            if (inputHandler.isKeyPressed(KeyEvent.VK_A)) {
                rocket2.move(-moveSpeed, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            }
            if (inputHandler.isKeyPressed(KeyEvent.VK_D)) {
                rocket2.move(moveSpeed, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            }

            // Mouse shooting
            mouseShooting = inputHandler.isMouseButtonPressed();

            // Move rocket 2 with mouse position
            int mouseX = inputHandler.getMouseX();
            int mouseY = inputHandler.getMouseY();

            // Smooth movement towards mouse
            float targetX = Math.max(0, Math.min(Constants.WINDOW_WIDTH - rocket2.getWidth(), mouseX - rocket2.getWidth()/2));
            float targetY = Math.max(0, Math.min(Constants.WINDOW_HEIGHT - rocket2.getHeight(), mouseY - rocket2.getHeight()/2));

            // Current position
            float currentX = rocket2.getX();
            float currentY = rocket2.getY();

            // Move towards target
            float dx = (targetX - currentX) * 0.1f;
            float dy = (targetY - currentY) * 0.1f;
            rocket2.move(dx, dy, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

            // Shoot with mouse
            shootWithMouse();
        }

        // Common controls
        if (inputHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            gameState.togglePause();
        }

        if (inputHandler.isKeyPressed(KeyEvent.VK_M)) {
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
            int obstacleCount = Math.min(baseCount + extraCount, 8);

            for (int i = 0; i < obstacleCount; i++) {
                spawnObstacle();
            }
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
    }

    private void spawnEnemy() {
        float x = (float) (Math.random() * (Constants.WINDOW_WIDTH - 40));
        float y = -50;
        float speed = Constants.ENEMY_BASE_SPEED * gameState.getDifficulty();

        Enemy.EnemyType type;
        int currentLevel = gameState.getLevel();

        if (currentLevel == 1) {
            type = Enemy.EnemyType.BASIC;
        } else if (currentLevel == 2) {
            double rand = Math.random();
            if (rand < 0.4) type = Enemy.EnemyType.ZIGZAG;
            else if (rand < 0.7) type = Enemy.EnemyType.SHOOTER;
            else type = Enemy.EnemyType.BASIC;
        } else {
            Enemy.EnemyType[] types = Enemy.EnemyType.values();
            type = types[(int) (Math.random() * types.length)];
        }

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
        if (!gameState.hasBoss()) {
            checkLevelUp();
        }
    }

    private void checkLevelUp() {
        int scorePerLevel = 1500;
        int expectedLevel = (gameState.getScore() / scorePerLevel) + 1;

        if (gameState.isTwoPlayerMode()) {
            int expectedLevel2 = (gameState.getScorePlayer2() / scorePerLevel) + 1;
            expectedLevel = Math.max(expectedLevel, expectedLevel2);
        }

        if (expectedLevel > gameState.getLevel()) {
            if (gameState.getLevel() == 3) {
                levelThreeComplete = true;
                gameState.setVictory(true);
                gameState.setGameOver(true);
                gameState.clearAllEntities();
                System.out.println("LEVEL 3 COMPLETE! Game finished.");
            } else {
                startLevelTransition();
            }
        }
    }

    private void startLevelTransition() {
        levelTransitioning = true;
        levelTransitionStartTime = System.currentTimeMillis();
        gameState.clearAllEntities();

        if (gameState.getLevel() == 1) {
            showUpgradeMenu = false;
            selectedUpgrade = 0;
        } else {
            showUpgradeMenu = true;
            selectedUpgrade = 0;
            upgradeMenuStartTime = System.currentTimeMillis();
        }
    }

    private void handleLevelTransition(long currentTime) {
        long elapsed = currentTime - levelTransitionStartTime;

        if (elapsed > LEVEL_TRANSITION_DURATION) {
            levelTransitioning = false;

            if (gameState.getLevel() == 1) {
                gameState.levelUp();
                showUpgradeMenu = false;

                resetRocketPositions();
            }
            else if (gameState.getLevel() >= 2) {
                showUpgradeMenu = true;
                selectedUpgrade = 0;
                upgradeMenuStartTime = System.currentTimeMillis();
            }
        }
    }

    private void resetRocketPositions() {
        Rocket rocket1 = gameState.getRocket();
        if (rocket1 != null) {
            float targetX1 = 100;
            float targetY1 = Constants.WINDOW_HEIGHT / 2;

            float dx1 = targetX1 - rocket1.getX();
            float dy1 = targetY1 - rocket1.getY();

            rocket1.move(dx1, dy1, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

            int maxHealth = rocket1.getMaxHealth();
            rocket1.heal(maxHealth);
        }

        Rocket rocket2 = gameState.getRocket2();
        if (rocket2 != null) {
            float targetX2 = 150;
            float targetY2 = Constants.WINDOW_HEIGHT / 2;

            float dx2 = targetX2 - rocket2.getX();
            float dy2 = targetY2 - rocket2.getY();

            rocket2.move(dx2, dy2, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

            int maxHealth2 = rocket2.getMaxHealth();
            rocket2.heal(maxHealth2);
        }
    }

    private void handleUpgradeMenuInput() {
        if (inputHandler == null) return;

        long currentTime = System.currentTimeMillis();

        if (currentTime - upgradeMenuStartTime > 3000) {
            applyUpgrade(selectedUpgrade);
            showUpgradeMenu = false;
            levelTransitioning = false;

            gameState.levelUp();

            resetRocketPositions();
            return;
        }

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
            levelTransitioning = false;

            gameState.levelUp();

            resetRocketPositions();
        }
    }

    public void applyUpgrade(int upgradeIndex) {
        switch (upgradeIndex) {
            case 0: gameState.addLife(); break;
            case 1: gameState.upgradeDamage(); break;
            case 2: gameState.upgradeSpeed(); break;
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
        levelThreeComplete = false;
        gameOverTimestamp = 0;
    }

    public boolean isLevelThreeComplete() {
        return levelThreeComplete;
    }

    public int getLevelThreeMenuSelection() {
        return levelThreeMenuSelection;
    }

    public GameState getGameState() { return gameState; }
    public ParticleSystem getParticleSystem() { return particleSystem; }
    public boolean isShowingUpgradeMenu() { return showUpgradeMenu; }
    public String[] getUpgradeOptions() { return upgradeOptions; }
    public int getSelectedUpgrade() { return selectedUpgrade; }
    public boolean isLevelTransitioning() { return levelTransitioning; }
    public float getDeltaTime() { return deltaTime; }
}