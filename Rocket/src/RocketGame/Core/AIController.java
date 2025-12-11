package RocketGame.Core;

import RocketGame.Audio.SoundManager;
import RocketGame.Entities.Bullet;
import RocketGame.Entities.Enemy;
import RocketGame.Entities.GameObject;
import RocketGame.Entities.Obstacle;
import RocketGame.Entities.Rocket;
import RocketGame.Util.Constants;

public class AIController {
    private GameState gameState;
    private Rocket aiRocket;
    private SoundManager soundManager;

    private float reactionDistance = 200.0f;
    private float centeringSpeed = 0.5f;

    public AIController(GameState gameState) {
        this.gameState = gameState;
        this.aiRocket = gameState.getRocket2();
        this.soundManager = SoundManager.getInstance();
    }

    public void update() {
        if (aiRocket == null || aiRocket.isDead()) return;

        float aiSpeed = aiRocket.getSpeed() * gameState.getSpeedMultiplier();

        GameObject target = null;
        float minDistance = Float.MAX_VALUE;

        for (Enemy enemy : gameState.getEnemies()) {
            float dist = Math.abs(enemy.getX() - aiRocket.getX());
            if (dist < minDistance && enemy.getY() < aiRocket.getY()) {
                minDistance = dist;
                target = enemy;
            }
        }

        if (target == null && gameState.getBoss() != null && gameState.getBoss().isActive()) {
            target = gameState.getBoss();
        }

        if (target != null) {
            float xDistance = target.getX() + target.getWidth()/2 - (aiRocket.getX() + aiRocket.getWidth()/2);

            if (xDistance > 10) {
                aiRocket.move(aiSpeed * 0.8f, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            } else if (xDistance < -10) {
                aiRocket.move(-aiSpeed * 0.8f, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            }

            if (Math.abs(xDistance) < 60) {
                if (aiRocket.canShoot()) {
                    shoot();
                }
            }
        } else {
            returnToCenter(aiSpeed);
        }

        avoidObstacles(aiSpeed);
    }

    private void shoot() {
        aiRocket.shoot();
        soundManager.playShoot();

        float bx = aiRocket.getX() + aiRocket.getWidth()/2 - 3;
        float by = aiRocket.getY();
        int dmg = (int)(aiRocket.getDamage() * gameState.getDamageMultiplier());

        Bullet b = new Bullet(bx, by, dmg, "normal");
        b.setPlayerNumber(2);
        gameState.getBullets().add(b);
    }

    private void returnToCenter(float speed) {
        float centerX = Constants.WINDOW_WIDTH / 2 + 150;
        if (aiRocket.getX() < centerX - 20) {
            aiRocket.move(speed * centeringSpeed, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        } else if (aiRocket.getX() > centerX + 20) {
            aiRocket.move(-speed * centeringSpeed, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        }
    }

    private void avoidObstacles(float speed) {
        for (Obstacle obs : gameState.getObstacles()) {
            boolean isInFront = obs.getY() + obs.getHeight() > aiRocket.getY() - reactionDistance;
            boolean hasPassed = obs.getY() > aiRocket.getY() + aiRocket.getHeight();

            if (isInFront && !hasPassed) {
                float rocketCenter = aiRocket.getX() + aiRocket.getWidth()/2;
                float obsCenter = obs.getX() + obs.getWidth()/2;

                if (Math.abs(rocketCenter - obsCenter) < (aiRocket.getWidth() + obs.getWidth()) / 1.5) {
                    if (rocketCenter < obsCenter) {
                        aiRocket.move(-speed, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
                    } else {
                        aiRocket.move(speed, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
                    }
                }
            }
        }
    }
}