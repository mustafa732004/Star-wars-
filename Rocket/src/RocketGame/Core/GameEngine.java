package RocketGame.Core;

import RocketGame.Entities.*;
import RocketGame.Effects.ParticleSystem;
import RocketGame.Input.InputHandler;
import RocketGame.Audio.SoundManager;
import RocketGame.Util.*;
import RocketGame.Rendering.GameRenderer;
import java.awt.event.KeyEvent;
import java.util.List;

public class GameEngine {
    private GameState gameState;
    private CollisionManager collisionManager;
    private ParticleSystem particleSystem;
    private InputHandler inputHandler;
    private SoundManager soundManager;
    private GameRenderer gameRenderer;
    private long gameOverTimestamp = 0;
    private long lastUpdateTime;
    private float deltaTime;
    private long lastObstacleSpawn;
    private long lastEnemySpawn;
    private int obstacleSpawnInterval;
    private int enemySpawnInterval;
    private static final int MIN_OBSTACLE_INTERVAL = 500;
    private static final int MAX_OBSTACLE_INTERVAL = 2000;
    private boolean levelTransitioning = false;
    private long levelTransitionStartTime;
    private int levelTransitionDuration = 2000;
    private boolean showUpgradeMenu = false;
    private String[] upgradeOptions;
    private int selectedUpgrade;
    private long lastMouseShot = 0;
    private int currentLevelDifficulty;
    private boolean isMultiplayerMode;
    private boolean isAIMode;
    private AIController aiController;
    private GameTimeCalculator gameTimeCalculator;

    public GameEngine(InputHandler inputHandler, boolean isMultiplayer, boolean isAI, int level, String username, String username2) {
        this.inputHandler = inputHandler;
        this.isMultiplayerMode = isMultiplayer;
        this.currentLevelDifficulty = level;
        this.isAIMode = isAI;
        this.soundManager = SoundManager.getInstance();
        this.particleSystem = new ParticleSystem();
        this.gameState = new GameState();
        this.gameState.initialize(isMultiplayer, level);
        this.gameState.setUsername(username);
        this.gameState.setUsername2(username2);
        this.collisionManager = new CollisionManager(particleSystem);
        this.gameTimeCalculator = new GameTimeCalculator();

        if (isMultiplayer && isAI) {
            this.aiController = new AIController(gameState);
        }

        this.lastUpdateTime = System.currentTimeMillis();
        this.deltaTime = 0;
        this.obstacleSpawnInterval = 1000;
        this.enemySpawnInterval = 2000;
        this.lastObstacleSpawn = System.currentTimeMillis();
        this.lastEnemySpawn = System.currentTimeMillis();
        this.levelTransitioning = false;
        this.levelTransitionDuration = 2000;
        this.showUpgradeMenu = false;
        this.upgradeOptions = new String[]{"Add Life", "Damage Boost", "Speed Boost", "Fire Rate"};
        this.selectedUpgrade = 0;

        soundManager.startBackgroundMusic("background");
    }

    private float findObstacleX(float width) {
        List<Obstacle> obstacles = gameState.getObstacles();
        int maxAttempts = 15;
        float padding = 50;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            float proposedX = (float) (Math.random() * (Constants.WINDOW_WIDTH - width));
            boolean overlap = false;

            for (Obstacle obs : obstacles) {
                if (obs.getY() < 200) {
                    if (proposedX < obs.getX() + obs.getWidth() + padding &&
                            proposedX + width + padding > obs.getX()) {
                        overlap = true;
                        break;
                    }
                }
            }

            if (!overlap) {
                return proposedX;
            }
        }
        return -1;
    }

    public void resetGame() {
        restart();
    }

    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public void setGameRenderer(GameRenderer gameRenderer) {
        this.gameRenderer = gameRenderer;
    }

    public void initialization(InputHandler inputHandler) {
        initializeInputHandler(inputHandler);
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
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

        if (aiController != null) {
            aiController.update();
        }

        handleInput();
        updateEntities();
        spawnEntities();
        checkCollisions();
        updateGameLogic();
        cleanupEntities();
    }

    private void handlePauseInput() {
        if (inputHandler == null) return;

        if (inputHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            if (gameState.isPaused()) {
                gameTimeCalculator.endPause();
                soundManager.resumeBackgroundMusic();
            } else {

                gameTimeCalculator.startPause();
                soundManager.pauseBackgroundMusic();
            }
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

        if (inputHandler != null) {
            if (inputHandler.isKeyPressed(KeyEvent.VK_SPACE) || inputHandler.isKeyPressed(KeyEvent.VK_ENTER)) {
                Rocket rocket = gameState.getRocket();
                int finalScore = gameState.getScore();
                boolean isVictory = rocket.getHealth() > 0;
                recordGameStats(gameState.getUsername(), finalScore, isVictory);
                restart();
            }
        }
    }

    private void shootBullet() {
        Rocket rocket = gameState.getRocket();

        if (!rocket.canShoot()) return;

        rocket.shoot();
        soundManager.playShoot();

        float bulletX = rocket.getX() + rocket.getWidth() / 2 - 3;
        float bulletY = rocket.getY();
        int damage = (int) (rocket.getDamage() * gameState.getDamageMultiplier());

        Bullet bullet = new Bullet(bulletX, bulletY, damage, "normal");
        bullet.setPlayerNumber(1);
        gameState.getBullets().add(bullet);

        String weaponType = rocket.getWeaponType();

        if (weaponType.equals("spread")) {
            for (int i = -1; i <= 1; i++) {
                float angle = i * 15;
                Bullet bullet1 = new Bullet(bulletX, bulletY, damage, angle);
                gameState.getBullets().add(bullet1);
            }
        } else if (weaponType.equals("laser")) {
            Bullet bullet2 = new Bullet(bulletX, bulletY, damage * 2, "laser");
            gameState.getBullets().add(bullet2);
        } else {
            Bullet bullet3 = new Bullet(bulletX, bulletY, damage, "normal");
            gameState.getBullets().add(bullet3);
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
                List<Vector2D> positions = boss.getBulletSpawnPositions();
                for (int i = 0; i < positions.size(); i++) {
                    Vector2D pos = positions.get(i);
                    Bullet bossBullet = new Bullet(pos.x, pos.y, 15, true, "planet");
                    gameState.getBullets().add(bossBullet);
                }
            }
        }

        particleSystem.update(1.0f);
        gameState.updateTemporaryPowerups();

        if (Math.random() < 0.3) {
            Rocket rocket = gameState.getRocket();
            particleSystem.createEngineTrail(rocket.getX() + rocket.getWidth() / 2 - 2, rocket.getY() + rocket.getHeight());
        }

        if (gameState.getRocket2() != null && Math.random() < 0.3) {
            Rocket rocket2 = gameState.getRocket2();
            particleSystem.createEngineTrail(rocket2.getX() + rocket2.getWidth() / 2 - 2, rocket2.getY() + rocket2.getHeight());
        }
    }

    private void handleInput() {
        if (inputHandler == null) return;

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

        if (gameState.getRocket2() != null && !isAIMode) {
            Rocket rocket2 = gameState.getRocket2();
            moveSpeed = rocket2.getSpeed() * gameState.getSpeedMultiplier();

            if (inputHandler.isWPressed()) {
                rocket2.move(0, -moveSpeed, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            }
            if (inputHandler.isSPressed()) {
                rocket2.move(0, moveSpeed, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            }
            if (inputHandler.isAPressed()) {
                rocket2.move(-moveSpeed, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            }
            if (inputHandler.isDPressed()) {
                rocket2.move(moveSpeed, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            }

            float targetX = inputHandler.getMouseX() - rocket2.getWidth() / 2;
            float targetY = inputHandler.getMouseY() - rocket2.getHeight() / 2;
            float dx = (targetX - rocket2.getX()) * 0.1f;
            float dy = (targetY - rocket2.getY()) * 0.1f;
            rocket2.move(dx, dy, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

            if (inputHandler.isMouseButtonPressed()) {
                shootWithMouse(rocket2);
            }
        }

        if (inputHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            gameState.togglePause();
        }

        if (inputHandler.isKeyPressed(KeyEvent.VK_M)) {
            soundManager.toggleMute();
        }
    }

    private void shootWithMouse(Rocket rocket) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMouseShot < 200) return;

        if (rocket.canShoot()) {
            rocket.shoot();
            soundManager.playShoot();

            float bx = rocket.getX() + rocket.getWidth() / 2 - 3;
            float by = rocket.getY();
            int dmg = (int) (rocket.getDamage() * gameState.getDamageMultiplier());

            Bullet b = new Bullet(bx, by, dmg, "normal");
            gameState.getBullets().add(b);
            b.setPlayerNumber(2);

            lastMouseShot = currentTime;
        }
    }

    private void spawnEntities() {
        long currentTime = System.currentTimeMillis();

        if (gameState.hasBoss()) return;

        float difficulty = gameState.getDifficulty();
        int adjustedObstacleInterval = (int) (obstacleSpawnInterval / difficulty);
        int adjustedEnemyInterval = (int) (enemySpawnInterval / difficulty);

        adjustedObstacleInterval = Math.max(MIN_OBSTACLE_INTERVAL, adjustedObstacleInterval);
        adjustedEnemyInterval = Math.max(1500, adjustedEnemyInterval);

        if (currentTime - lastObstacleSpawn >= adjustedObstacleInterval) {
            int baseCount = 5;
            int extraCount = (int) (difficulty / 2);
            int obstacleCount = Math.min(baseCount + extraCount, 15);

            for (int i = 0; i < obstacleCount; i++) {
                spawnObstacle();
            }
            lastObstacleSpawn = currentTime;
        }

        if (currentTime - lastEnemySpawn >= adjustedEnemyInterval) {
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
        float x = findObstacleX(width);

        if (x == -1) return;

        float y = -height;
        float speed = Constants.ENEMY_BASE_SPEED * gameState.getDifficulty();

        Obstacle obstacle = new Obstacle(x, y, width, height, speed);
        gameState.getObstacles().add(obstacle);
    }

    private float findEnemyPosition(float width, float height, boolean isShooter) {
        List<Enemy> enemies = gameState.getEnemies();
        float y = -height;
        float minSpacing = isShooter ? 120 : 60;
        int maxAttempts = isShooter ? 40 : 20;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            float x = (float) (Math.random() * (Constants.WINDOW_WIDTH - width));
            boolean validPosition = true;

            for (Enemy existing : enemies) {
                float centerX1 = x + width / 2;
                float centerX2 = existing.getX() + existing.getWidth() / 2;
                float distance = Math.abs(centerX1 - centerX2);

                float verticalDistance = Math.abs(y - existing.getY());

                if (isShooter) {
                    if (existing.getType() == Enemy.EnemyType.SHOOTER) {
                        if (distance < minSpacing) {
                            validPosition = false;
                            break;
                        }
                    }
                } else {
                    if (verticalDistance < 100) {
                        if (distance < width + existing.getWidth() / 2 + minSpacing / 2) {
                            validPosition = false;
                            break;
                        }
                    }
                }
            }

            if (validPosition) {
                return x;
            }
        }

        for (int attempt = 0; attempt < 10; attempt++) {
            float x = (float) (Math.random() * (Constants.WINDOW_WIDTH - width));
            boolean validPosition = true;

            for (Enemy existing : enemies) {
                if (isShooter && existing.getType() == Enemy.EnemyType.SHOOTER) {
                    float centerX1 = x + width / 2;
                    float centerX2 = existing.getX() + existing.getWidth() / 2;
                    if (Math.abs(centerX1 - centerX2) < 60) {
                        validPosition = false;
                        break;
                    }
                }
            }

            if (validPosition) {
                System.out.println("SPAWN: Using minimal spacing");
                return x;
            }
        }

        return -1;
    }

    private void spawnEnemy() {
        float width = 40;
        float height = 40;
        float y = -height;
        float speed = Constants.ENEMY_BASE_SPEED * gameState.getDifficulty();
        boolean spawnShooter = Math.random() < 0.75;

        float x = findEnemyPosition(width, height, spawnShooter);
        if (x < 0) return;

        Enemy.EnemyType type;

        if (spawnShooter) {
            type = Enemy.EnemyType.SHOOTER;
        } else {
            Enemy.EnemyType[] otherTypes = {Enemy.EnemyType.BASIC, Enemy.EnemyType.ZIGZAG, Enemy.EnemyType.KAMIKAZE};
            type = otherTypes[(int) (Math.random() * otherTypes.length)];
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
        int totalScore = gameState.getScore() + gameState.getScorePlayer2();
        int expectedLevel = totalScore / scorePerLevel + 1;

        if (gameState.getLevel() >= 3) return;

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

        if (elapsed >= levelTransitionDuration) {
            levelTransitioning = false;
            gameState.levelUp();
        }
    }

    private void handleUpgradeMenuInput() {
        if (inputHandler == null) return;

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

        if (inputHandler.isKeyPressed(KeyEvent.VK_SPACE) || inputHandler.isKeyPressed(KeyEvent.VK_ENTER)) {
            applyUpgrade(selectedUpgrade);
            showUpgradeMenu = false;
        }
    }

    public void applyUpgrade(int upgradeIndex) {
        switch (upgradeIndex) {
            case 0:
                gameState.addLife();
                break;
            case 1:
                gameState.upgradeDamage();
                break;
            case 2:
                gameState.upgradeSpeed();
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
        gameState.reset(currentLevelDifficulty);
        particleSystem.clear();
        lastObstacleSpawn = System.currentTimeMillis();
        lastEnemySpawn = System.currentTimeMillis();
        levelTransitioning = false;
        showUpgradeMenu = false;
        gameOverTimestamp = 0;
        gameTimeCalculator.reset();


        soundManager.resumeBackgroundMusic();
    }

    public void initializeInputHandler(InputHandler inputHandler) {
        if (gameRenderer != null && inputHandler != null) {
            inputHandler.setGameRenderer(gameRenderer);
            inputHandler.setSoundManager(soundManager);
            System.out.println("INIT: InputHandler, GameRenderer, SoundManager");
        }
    }

    public void recordGameStats(String username, int score, boolean isVictory) {
        long playTimeSeconds = gameTimeCalculator.getGameTimeInSeconds();
        UserStatsManager statsManager = new UserStatsManager();
        statsManager.updatePlayerStats(username, score, playTimeSeconds, isVictory);
        statsManager.printLeaderboard();

        System.out.println("GAME OVER: " + gameTimeCalculator.getFormattedGameTime());
        System.out.println("GAME OVER SCORE: " + score);
        //3ash
        System.out.println("GAME OVER: " + (isVictory ? "VICTORY" : "DEFEAT"));
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

    public GameTimeCalculator getGameTimeCalculator() {
        return gameTimeCalculator;
    }
}