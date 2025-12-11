package RocketGame.Core;

import RocketGame.Entities.*;
import RocketGame.Effects.ParticleSystem;
import RocketGame.Audio.SoundManager;
import RocketGame.Util.Constants;
import java.util.List;

public class CollisionManager {
    private ParticleSystem particleSystem;
    private SoundManager soundManager;

    public CollisionManager(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
        this.soundManager = SoundManager.getInstance();
    }

    public void checkAllCollisions(GameState gameState) {
        Rocket rocket = gameState.getRocket();
        Rocket rocket2 = gameState.getRocket2();

        List<Bullet> bullets = gameState.getBullets();
        List<Enemy> enemies = gameState.getEnemies();
        List<Obstacle> obstacles = gameState.getObstacles();
        List<PowerUp> powerups = gameState.getPowerups();
        Boss boss = gameState.getBoss();

        checkBulletCollisions(bullets, enemies, obstacles, boss, gameState);
        checkRocketCollisions(rocket, enemies, obstacles, boss, gameState);
        checkPowerupCollisions(rocket, powerups, gameState);

        if (rocket2 != null) {
            checkRocketCollisions(rocket2, gameState.getEnemies(), gameState.getObstacles(), gameState.getBoss(), gameState);
            checkPowerupCollisions(rocket2, gameState.getPowerups(), gameState);
        }
    }

    private void checkBulletCollisions(List<Bullet> bullets, List<Enemy> enemies,
                                       List<Obstacle> obstacles, Boss boss, GameState gameState) {

        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            boolean bulletHit = false;

            if (bullet.isFromEnemy()) {
                continue;
            }

            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (bullet.collidesWith(enemy)) {
                    enemy.takeDamage(bullet.getDamage());
                    particleSystem.createHitEffect(bullet.getX(), bullet.getY(), new float[]{1.0f, 1.0f, 0.0f});
                    soundManager.playHit();

                    if (enemy.isDestroyed()) {
                        particleSystem.createLargeExplosion(enemy.getX() + enemy.getWidth() / 2, enemy.getY() + enemy.getHeight() / 2, new float[]{1.0f, 0.5f, 0.0f});
                        soundManager.playExplosion();
                        if (bullet.getPlayerNumber() == 2) {
                            gameState.addScorePlayer2(Constants.SCORE_ENEMY_DESTROY);
                        } else {
                            gameState.addScore(Constants.SCORE_ENEMY_DESTROY);
                        }
                        gameState.incrementCombo();
                        enemies.remove(j);
                        if (Math.random() < 0.3) {
                            gameState.getPowerups().add(new PowerUp(enemy.getX(), enemy.getY()));
                        }
                    }
                    bullets.remove(i);
                    bulletHit = true;
                    break;
                }
            }
            if (bulletHit) continue;

            for (int j = obstacles.size() - 1; j >= 0; j--) {
                Obstacle obstacle = obstacles.get(j);
                if (bullet.collidesWith(obstacle)) {
                    obstacle.takeDamage(bullet.getDamage());
                    float[] color = {0.8f, 0.6f, 0.8f};
                    particleSystem.createHitEffect(bullet.getX(), bullet.getY(), color);
                    soundManager.playHit();

                    if (obstacle.isDestroyed()) {
                        particleSystem.createLargeExplosion(
                                obstacle.getX() + obstacle.getWidth() / 2,
                                obstacle.getY() + obstacle.getHeight() / 2,
                                obstacle.getColor()
                        );
                        soundManager.playExplosion();
                        if (bullet.getPlayerNumber() == 2) {
                            gameState.addScorePlayer2(Constants.SCORE_OBSTACLE_DESTROY);
                        } else {
                            gameState.addScore(Constants.SCORE_OBSTACLE_DESTROY);
                        }
                        gameState.incrementCombo();
                        obstacles.remove(j);
                    }
                    bullets.remove(i);
                    bulletHit = true;
                    break;
                }
            }
            if (bulletHit) continue;

            if (boss != null && boss.isActive()) {
                if (bullet.collidesWith(boss)) {
                    boss.takeDamage(bullet.getDamage());
                    particleSystem.createHitEffect(bullet.getX(), bullet.getY(), new float[]{1.0f, 0.5f, 0.0f});
                    soundManager.playHit();

                    if (boss.isDefeated()) {
                        System.out.println("BOSS DEFEATED - TRIGGERING VICTORY"); // Debug print

                        particleSystem.createBossExplosion(boss.getX() + boss.getWidth() / 2, boss.getY() + boss.getHeight() / 2);
                        soundManager.playExplosion();
                        if (bullet.getPlayerNumber() == 2) {
                            gameState.addScorePlayer2(Constants.SCORE_BOSS_DESTROY);
                        } else {
                            gameState.addScore(Constants.SCORE_BOSS_DESTROY);
                        }

                        gameState.setBoss(null);

                        gameState.setVictory(true);
                        gameState.setGameOver(true);
                    }

                    bullets.remove(i);
                    bulletHit = true;
                }
            }
        }
    }

    private void checkRocketCollisions(Rocket rocket, List<Enemy> enemies,
                                       List<Obstacle> obstacles, Boss boss, GameState gameState) {
        if (rocket.isInvincible()) return;

        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (rocket.collidesWith(enemy)) {
                handleRocketDamage(rocket, 25, gameState);
                particleSystem.createExplosion(enemy.getX() + enemy.getWidth()/2, enemy.getY() + enemy.getHeight()/2, new float[]{1.0f, 0.3f, 0.3f}, 20);
                enemies.remove(i);
            }
        }

        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            if (rocket.collidesWith(obstacle)) {
                handleRocketDamage(rocket, 20, gameState);
                particleSystem.createExplosion(obstacle.getX() + obstacle.getWidth()/2, obstacle.getY() + obstacle.getHeight()/2, new float[]{1.0f, 0.3f, 0.3f}, 15);
                obstacles.remove(i);
            }
        }

        if (boss != null && boss.isActive()) {
            if (rocket.collidesWith(boss)) {
                handleRocketDamage(rocket, 30, gameState);
                particleSystem.createExplosion(rocket.getX() + rocket.getWidth()/2, rocket.getY() + rocket.getHeight()/2, new float[]{1.0f, 0.0f, 0.0f}, 25);
            }
        }

        List<Bullet> bullets = gameState.getBullets();
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            if (bullet.isFromEnemy() && rocket.collidesWith(bullet)) {
                handleRocketDamage(rocket, bullet.getDamage(), gameState);
                particleSystem.createHitEffect(bullet.getX(), bullet.getY(), new float[]{1.0f, 0.5f, 0.0f});
                bullets.remove(i);
            }
        }
    }

    private void handleRocketDamage(Rocket rocket, int damage, GameState gameState) {
        rocket.takeDamage(damage);
        soundManager.playDamage();

        if (rocket.isDead()) {
            if (rocket == gameState.getRocket()) {
                gameState.loseLife();
            } else if (rocket == gameState.getRocket2()) {
                gameState.loseLifePlayer2();
            }
            particleSystem.createLargeExplosion(rocket.getX() + rocket.getWidth() / 2, rocket.getY() + rocket.getHeight() / 2, new float[]{1.0f, 0.0f, 0.0f});
            soundManager.playExplosion();
            gameState.resetCombo();

            boolean p1Alive = (rocket == gameState.getRocket() && gameState.getLives() > 0);
            boolean p2Alive = (rocket == gameState.getRocket2() && gameState.getLivesPlayer2() > 0);

            if (p1Alive || p2Alive) {
                rocket.heal(rocket.getMaxHealth());
                rocket.setInvincible(3000);
            }
        }
    }

    private void checkPowerupCollisions(Rocket rocket, List<PowerUp> powerups, GameState gameState) {
        for (int i = powerups.size() - 1; i >= 0; i--) {
            PowerUp powerup = powerups.get(i);
            if (rocket.collidesWith(powerup)) {
                powerup.applyToRocket(rocket);
                if (powerup.getType() == PowerUp.PowerupType.COIN) {
                    gameState.addScore(powerup.getScoreValue());
                } else if (powerup.givesExtraLife()) {
                    gameState.setLives(gameState.getLives() + 1);
                }
                float[] color = new float[]{0.0f, 1.0f, 0.0f};
                particleSystem.createPowerupEffect(powerup.getX() + powerup.getWidth() / 2, powerup.getY() + powerup.getHeight() / 2, color);
                soundManager.playPowerup();
                powerups.remove(i);
            }
        }
    }

    public void updateCombo(GameState gameState) {
        if (gameState.getCombo() > 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - gameState.getComboTimer() > 3000) {
                gameState.resetCombo();
            }
        }
    }

    public void setParticleSystem(ParticleSystem particleSystem) { this.particleSystem = particleSystem; }
    public void setSoundManager(SoundManager soundManager) { this.soundManager = soundManager; }
}
