package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Enemy {
    private float x;
    private float y;
    private float speed = 60f;
    private boolean facingRight = true;
    private float patrolLeft;
    private float patrolRight;
    private Rectangle bounds = new Rectangle();

    private Animation<TextureRegion> runAnim;
    private float stateTime = 0f;

    public static final int FRAME_WIDTH = 57;
    public static final int FRAME_HEIGHT = 60;

    private int maxHealth = 3;
    private int currentHealth= 3;
    private boolean isDead = false;
    private float invincibilityTimer = 0f;
    private static final float INVINCIBILITY_DURATION = 0.3f;

    //constructor
    public Enemy(float x, float y, float patrolLeft, float patrolRight) {
        this.x = x;
        this.y = y;
        this.patrolLeft= patrolLeft;
        this.patrolRight = patrolRight;

        runAnim = loadAnimation("Assets/SPRITES/burning-ghoul/run 1/spritesheet.png", 8, 0.1f);
        bounds.setSize(32, 32);
        bounds.setPosition(x, y);
    }

    private Animation<TextureRegion> loadAnimation(String path, int cols, float frameDuration) {
        Texture sheet = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight());
        Array<TextureRegion> frames = new Array<>();
        for (int i=0; i < cols; i++) {
            frames.add(tmp[0][i]);
        }
        return new Animation<>(frameDuration, frames);
    }

    public void update(float delta) {
        // movement in current direction
        if (facingRight) {
            x += speed * delta;
        } else {
            x -= speed * delta;
        }

        // flipping at patrol boundary
        if (x >= patrolRight) {
            facingRight = false;
        } else if (x <= patrolLeft) {
            facingRight = true;
        }

        // keeping bounds synced
        bounds.setPosition(x, y);
        stateTime += delta;

        //invincibility timer
        if (invincibilityTimer > 0) {
            invincibilityTimer -= delta;
        }
    }

    public TextureRegion getCurrentFrame() {
        // simpler than in player because we have only one animation
        TextureRegion frame = runAnim.getKeyFrame(stateTime, true);
        if (!facingRight && frame.isFlipX()) {
            frame.flip(true, false);
        } else if (facingRight && !frame.isFlipX()) {
            frame.flip(true, false);
        }
        return frame;
    }

    public void takeDamage(int amount) {
        if (invincibilityTimer <= 0) {
            currentHealth -= amount;
            invincibilityTimer = INVINCIBILITY_DURATION;
            if (currentHealth <= 0) {
                isDead = true;
            }
        }
    }

    //getters
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isDead() {
        return isDead;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }
}
