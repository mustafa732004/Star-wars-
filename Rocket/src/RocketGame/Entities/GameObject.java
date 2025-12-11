package RocketGame.Entities;
import javax.media.opengl.*;
import RocketGame.Util.Vector2D;

public abstract class GameObject {
    protected Vector2D position;
    protected Vector2D velocity;
    protected float width;
    protected float height;
    protected boolean active;

    public GameObject(float x, float y, float width, float height) {
        this.position = new Vector2D(x, y);
        this.velocity = new Vector2D(0, 0);
        this.width = width;
        this.height = height;
        this.active = true;
    }


    public abstract void update(float deltaTime);
    public abstract void render(GL gl);


    public boolean collidesWith(GameObject object) {
        return this.position.x < object.position.x + object.width &&
                this.position.x + this.width > object.position.x &&
                this.position.y < object.position.y + object.height &&
                this.position.y + this.height > object.position.y;
    }

    public boolean isOutOfBounds(int screenWidth, int screenHeight) {
        return position.x + width < 0 ||
                position.x > screenWidth ||
                position.y + height < 0 ||
                position.y > screenHeight;
    }


    public Vector2D getPosition() { return position; }
    public void setPosition(float x, float y) { position.set(x, y); }

    public Vector2D getVelocity() { return velocity; }
    public void setVelocity(float x, float y) { velocity.set(x, y); }

    public float getX() { return position.x; }
    public float getY() { return position.y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public void destroy() { this.active = false; }
}