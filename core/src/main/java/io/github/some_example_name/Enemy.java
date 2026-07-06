package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Represents the Burning Ghoul patrol enemy in Heedless.
 * The ghoul moves back and forth between two patrol boundaries at a fixed speed,
 * dealing damage to the player on contact. It has a health system with brief
 * invincibility frames after being hit, and dies when health reaches zero.
 * Supports state restoration for the frame-perfect replay system.
 */
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

    /** Width of a single animation frame in pixels. */
    public static final int FRAME_WIDTH = 57;

    /** Height of a single animation frame in pixels. */
    public static final int FRAME_HEIGHT = 60;

    private int maxHealth = 3;
    private int currentHealth = 3;
    private boolean isDead = false;
    private float invincibilityTimer = 0f;
    private static final float INVINCIBILITY_DURATION = 0.3f;

    /**
     * Creates a new Enemy at the specified position with defined patrol boundaries.
     *
     * @param x           starting x position in pixels
     * @param y           y position in pixels (fixed, enemy does not move vertically)
     * @param patrolLeft  leftmost x boundary of the patrol range in pixels
     * @param patrolRight rightmost x boundary of the patrol range in pixels
     */
    public Enemy(float x, float y, float patrolLeft, float patrolRight) {
        this.x = x;
        this.y = y;
        this.patrolLeft = patrolLeft;
        this.patrolRight = patrolRight;

        runAnim = loadAnimation(
            "Assets/SPRITES/burning-ghoul/run 1/spritesheet.png", 8, 0.1f);
        bounds.setSize(32, 32);
        bounds.setPosition(x, y);
    }

    /**
     * Loads a sprite sheet and splits it into an Animation.
     *
     * @param path          internal asset path to the sprite sheet PNG
     * @param cols          number of columns (frames) in the sprite sheet
     * @param frameDuration duration of each frame in seconds
     * @return the constructed Animation object
     */
    private Animation<TextureRegion> loadAnimation(String path, int cols, float frameDuration) {
        Texture sheet = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tmp = TextureRegion.split(sheet,
            sheet.getWidth() / cols, sheet.getHeight());
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < cols; i++) {
            frames.add(tmp[0][i]);
        }
        return new Animation<>(frameDuration, frames);
    }

    /**
     * Updates the enemy's position, direction, animation timer and invincibility cooldown.
     * The enemy moves horizontally within its patrol boundaries, flipping direction
     * when it reaches either boundary.
     *
     * @param delta time elapsed since the last frame in seconds
     */
    public void update(float delta) {
        // movement in current direction
        if (facingRight) {
            x += speed * delta;
        } else {
            x -= speed * delta;
        }

        // flip at patrol boundaries
        if (x >= patrolRight) {
            facingRight = false;
        } else if (x <= patrolLeft) {
            facingRight = true;
        }

        // keep bounds synced with position
        bounds.setPosition(x, y);
        stateTime += delta;

        // count down invincibility after being hit
        if (invincibilityTimer > 0) {
            invincibilityTimer -= delta;
        }
    }

    /**
     * Returns the current animation frame, flipped horizontally based on movement direction.
     *
     * @return the current {@link TextureRegion} frame to render
     */
    public TextureRegion getCurrentFrame() {
        TextureRegion frame = runAnim.getKeyFrame(stateTime, true);
        if (!facingRight && frame.isFlipX()) {
            frame.flip(true, false);
        } else if (facingRight && !frame.isFlipX()) {
            frame.flip(true, false);
        }
        return frame;
    }

    /**
     * Restores the enemy's state directly from a recorded {@link FrameState} snapshot.
     * Used by the replay system to achieve frame-perfect playback without physics simulation.
     *
     * @param fs the frame state snapshot to restore from
     */
    public void restoreState(FrameState fs) {
        this.x = fs.enemyX;
        this.isDead = fs.enemyDead;
        this.currentHealth = fs.enemyHealth;
        this.bounds.setPosition(x, y);
    }

    /**
     * Advances the animation timer without updating position or logic.
     * Used during replay playback where position is restored from recorded state,
     * not simulated, to keep the animation visually smooth.
     *
     * @param delta time elapsed since the last frame in seconds
     */
    public void advanceAnimation(float delta) {
        stateTime += delta;
    }

    /**
     * Applies damage to the enemy if not currently invincible.
     * Triggers a brief invincibility window after each successful hit.
     * Sets {@code isDead} to true when health reaches zero.
     *
     * @param amount the amount of damage to apply
     */
    public void takeDamage(int amount) {
        if (invincibilityTimer <= 0) {
            currentHealth -= amount;
            invincibilityTimer = INVINCIBILITY_DURATION;
            if (currentHealth <= 0) {
                isDead = true;
            }
        }
    }

    /**
     * Returns the current x position of the enemy in the game world.
     *
     * @return x position in pixels
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the current y position of the enemy in the game world.
     *
     * @return y position in pixels
     */
    public float getY() {
        return y;
    }

    /**
     * Returns the collision bounds of the enemy.
     *
     * @return the {@link Rectangle} representing the enemy's hitbox
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Returns whether the enemy has been defeated.
     *
     * @return true if the enemy's health has reached zero
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Returns the enemy's current health points.
     *
     * @return current health value
     */
    public int getCurrentHealth() {
        return currentHealth;
    }
}
