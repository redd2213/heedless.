package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Represents the player character in Heedless.
 * Handles movement, gravity, tile-based AABB collision, jumping, melee attacking,
 * health management, knockback physics, and sprite animation with state machine.
 * Also supports direct state restoration for the frame-perfect replay system
 * via {@link #restoreState(FrameState)}.
 */
public class Player {
    private float x = 100;
    private float y = 100;
    private float speed = 200;
    private float velocityY = 0;
    private float velocityX = 0f;
    private boolean isGrounded = false;
    private Rectangle bounds = new Rectangle();
    private static final float GRAVITY = -900f;
    private static final float JUMP_VELOCITY = 430f;
    private float invincibilityTimer = 0f;
    private static final float INVINCIBILITY_DURATION = 0.0f; // TODO: set back to 1.5f before presentation
    private int maxHealth = 5;
    private int currentHealth = 5;
    private Rectangle attackHitbox = new Rectangle();
    private float attackTimer = 0f;
    private boolean isAttacking = false;
    private static final float ATTACK_DURATION = 0.48f;
    private static final float ATTACK_RANGE = 50f;

    /** Possible animation and logic states for the player. */
    public enum State { IDLE, WALK, JUMP, FALL, PUNCH }

    private State currentState = State.IDLE;
    private float stateTime = 0f;
    private boolean facingRight = true;

    /** Width of a single animation frame in pixels. */
    public static final int FRAME_WIDTH = 82;

    /** Height of a single animation frame in pixels. */
    public static final int FRAME_HEIGHT = 60;

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> jumpAnim;
    private Animation<TextureRegion> fallAnim;
    private Animation<TextureRegion> punchAnim;

    /**
     * Creates a new Player at the default starting position.
     * Loads all animation sprite sheets from assets.
     */
    public Player() {
        idleAnim = loadAnimation("Assets/SPRITES/player/Idle/spritesheet.png", 4, 0.15f);
        walkAnim = loadAnimation("Assets/SPRITES/player/Walk/spritesheet.png", 6, 0.1f);
        jumpAnim = loadAnimation("Assets/SPRITES/player/jump/spritesheet.png", 2, 0.1f);
        fallAnim = loadAnimation("Assets/SPRITES/player/fall/spritesheet.png", 2, 0.15f);
        punchAnim = loadAnimation("Assets/SPRITES/player/Punch/spritesheet.png", 6, 0.08f);

        bounds.setSize(32, 32);
        bounds.setPosition(x, y);
    }

    /**
     * Updates all player logic for this frame including movement, collision,
     * gravity, jumping, knockback, invincibility countdown, attacking, and animation state.
     * Called every frame during gameplay by {@link GameScreen}.
     *
     * @param delta the time elapsed since the last frame in seconds
     * @param layer the tile layer used for solid collision detection
     */
    public void update(float delta, TiledMapTileLayer layer) {
        isGrounded = false;
        float oldX = x;

        // horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += speed * delta;
            facingRight = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= speed * delta;
            facingRight = false;
        }

        bounds.setPosition(x, y);
        if (isColliding(layer)) {
            x = oldX;
            bounds.setPosition(x, y);
        }

        // apply knockback velocity and dampen it
        x += velocityX * delta;
        velocityX *= 0.9f;

        // count down invincibility timer
        if (invincibilityTimer > 0) {
            invincibilityTimer -= delta;
        }

        // apply gravity and resolve Y collision
        velocityY += GRAVITY * delta;
        float oldY = y;
        y += velocityY * delta;

        bounds.setPosition(x, y);
        if (isColliding(layer)) {
            if (velocityY < 0) {
                isGrounded = true;
            }
            y = oldY;
            velocityY = 0;
        }

        // jumping
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && isGrounded) {
            velocityY = JUMP_VELOCITY;
        }

        // update animation state
        if (isAttacking) {
            currentState = State.PUNCH;
        } else if (!isGrounded && velocityY > 0) {
            currentState = State.JUMP;
        } else if (!isGrounded && velocityY < 0) {
            currentState = State.FALL;
        } else if (x != oldX) {
            currentState = State.WALK;
        } else {
            currentState = State.IDLE;
        }

        // attacking
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isAttacking) {
            isAttacking = true;
            attackTimer = ATTACK_DURATION;
            stateTime = 0f;
        }

        if (isAttacking) {
            attackTimer -= delta;
            if (facingRight) {
                attackHitbox.set(x + 32, y, ATTACK_RANGE, 32);
            } else {
                attackHitbox.set(x - 32, y, ATTACK_RANGE, 32);
            }
            if (attackTimer <= 0) {
                isAttacking = false;
                attackHitbox.set(0, 0, 0, 0);
            }
        }
    }

    /**
     * Checks whether the player's current bounding box overlaps any solid tile.
     * Checks all tiles the rectangle could overlap, not just the center point,
     * to handle cases where the player straddles tile boundaries.
     *
     * @param layer the tile layer to check against
     * @return true if any solid tile is overlapping the player's bounds
     */
    private boolean isColliding(TiledMapTileLayer layer) {
        int startX = (int)(bounds.x / layer.getTileWidth());
        int endX = (int)((bounds.x + bounds.width) / layer.getTileWidth());
        int startY = (int)(bounds.y / layer.getTileHeight());
        int endY = (int)((bounds.y + bounds.height) / layer.getTileHeight());

        for (int tileY = startY; tileY <= endY; tileY++) {
            for (int tileX = startX; tileX <= endX; tileX++) {
                TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
                if (cell != null && cell.getTile() != null
                    && cell.getTile().getProperties().containsKey("solid")) {
                    return true;
                }
            }
        }
        return false;
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
     * Returns the current animation frame and advances the animation timer.
     * Selects the correct animation based on the current state.
     * Flips the frame horizontally based on the direction the player is facing.
     * The punch animation does not loop.
     *
     * @param delta time elapsed since last frame in seconds
     * @return the current {@link TextureRegion} frame to render
     */
    public TextureRegion getCurrentFrame(float delta) {
        stateTime += delta;

        Animation<TextureRegion> anim;
        switch (currentState) {
            case WALK:  anim = walkAnim;  break;
            case JUMP:  anim = jumpAnim;  break;
            case FALL:  anim = fallAnim;  break;
            case PUNCH: anim = punchAnim; break;
            default:    anim = idleAnim;  break;
        }

        TextureRegion frame = anim.getKeyFrame(stateTime, true);

        if (facingRight && frame.isFlipX()) frame.flip(true, false);
        else if (!facingRight && !frame.isFlipX()) frame.flip(true, false);

        return frame;
    }

    /**
     * Applies a horizontal and vertical knockback force to the player.
     * Used when the player is hit by an enemy.
     *
     * @param directionX the direction of the knockback: 1 for right, -1 for left
     */
    public void applyKnockback(float directionX) {
        velocityX = directionX * 1000f;
        velocityY = 100f;
    }

    /**
     * Restores the player's complete state from a recorded {@link FrameState} snapshot.
     * Used by the replay system to achieve frame-perfect playback without physics simulation.
     *
     * @param fs the frame state snapshot to restore from
     */
    public void restoreState(FrameState fs) {
        this.x = fs.playerX;
        this.y = fs.playerY;
        this.velocityY = fs.velocityY;
        this.velocityX = fs.velocityX;
        this.isGrounded = fs.isGrounded;
        this.currentState = fs.animState;
        this.facingRight = fs.facingRight;
        this.stateTime = fs.stateTime;
        this.currentHealth = fs.playerHealth;
        this.bounds.setPosition(x, y);
    }

    /**
     * Returns the current animation frame without advancing the animation timer.
     * Used during replay playback where the recorded {@code stateTime} is restored
     * directly from the frame state, ensuring frame-accurate visual reproduction.
     *
     * @return the current {@link TextureRegion} frame based on recorded state
     */
    public TextureRegion getCurrentFrameForReplay() {
        Animation<TextureRegion> anim;
        switch (currentState) {
            case WALK:  anim = walkAnim;  break;
            case JUMP:  anim = jumpAnim;  break;
            case FALL:  anim = fallAnim;  break;
            case PUNCH: anim = punchAnim; break;
            default:    anim = idleAnim;  break;
        }

        boolean loop = currentState != State.PUNCH;
        TextureRegion frame = anim.getKeyFrame(stateTime, loop);

        if (facingRight && frame.isFlipX()) frame.flip(true, false);
        else if (!facingRight && !frame.isFlipX()) frame.flip(true, false);

        return frame;
    }

    /**
     * Returns whether the player is currently in the post-hit invincibility window.
     *
     * @return true if the player cannot be damaged
     */
    public boolean isInvincible() {
        return invincibilityTimer > 0;
    }

    /**
     * Starts the invincibility timer, preventing damage for {@code INVINCIBILITY_DURATION} seconds.
     */
    public void triggerInvincibility() {
        invincibilityTimer = INVINCIBILITY_DURATION;
    }

    /** @return current x position in pixels */
    public float getX() { return x; }

    /** @return current y position in pixels */
    public float getY() { return y; }

    /** @return the player's collision hitbox */
    public Rectangle getBounds() { return bounds; }

    /** @return movement speed in pixels per second */
    public float getSpeed() { return speed; }

    /** @return current vertical velocity in pixels per second */
    public float getVelocityY() { return velocityY; }

    /** @return current horizontal velocity in pixels per second */
    public float getVelocityX() { return velocityX; }

    /** @return current animation and logic state */
    public Player.State getCurrentState() { return currentState; }

    /** @return true if the player is facing right */
    public boolean isFacingRight() { return facingRight; }

    /** @return current animation timer value in seconds */
    public float getStateTime() { return stateTime; }

    /** @return true if the player is standing on solid ground */
    public boolean isGrounded() { return isGrounded; }

    /** @return true if the player is currently performing a melee attack */
    public boolean isAttacking() { return isAttacking; }

    /** @return the active melee attack hitbox rectangle */
    public Rectangle getAttackHitbox() { return attackHitbox; }

    /** @return current health points */
    public int getCurrentHealth() { return currentHealth; }

    /**
     * Returns whether the player has been defeated.
     *
     * @return true if current health is zero or below
     */
    public boolean isDead() { return currentHealth <= 0; }

    /**
     * Applies damage to the player if not currently invincible.
     * Automatically triggers the invincibility window after a successful hit.
     *
     * @param amount the amount of health points to subtract
     */
    public void takeDamage(int amount) {
        if (!isInvincible()) {
            currentHealth -= amount;
            triggerInvincibility();
        }
    }
}
