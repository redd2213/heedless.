package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import org.w3c.dom.Text;

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
    private static final float INVINCIBILITY_DURATION = 0.0f; //switched to lower invincibility duration for damage testing purposes.
    private int maxHealth = 5;
    private int currentHealth = 5;
    private Rectangle attackHitbox = new Rectangle();
    private float attackTimer=0f;
    private boolean isAttacking = false;
    private static final float ATTACK_DURATION = 0.48f;
    private static final float ATTACK_RANGE = 50f;

    //animation fields
    public enum State { IDLE, WALK, JUMP, FALL, PUNCH }
    private State currentState = State.IDLE;
    private float stateTime = 0f;
    private boolean facingRight = true;
    public static final int FRAME_WIDTH = 82;
    public static final int FRAME_HEIGHT = 60;

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> jumpAnim;
    private Animation<TextureRegion> fallAnim;
    private Animation<TextureRegion> punchAnim;

    public Player() {
        idleAnim = loadAnimation("Assets/SPRITES/player/Idle/spritesheet.png", 4, 0.15f);
        walkAnim = loadAnimation("Assets/SPRITES/player/Walk/spritesheet.png", 6, 0.1f);
        jumpAnim = loadAnimation("Assets/SPRITES/player/Jump/spritesheet.png", 2, 0.1f);
        fallAnim = loadAnimation("Assets/SPRITES/player/Fall/spritesheet.png", 2, 0.15f);
        punchAnim = loadAnimation("Assets/SPRITES/player/Punch/spritesheet.png", 6, 0.08f);

        bounds.setSize(32, 32);
        bounds.setPosition(x, y);
    }

    public void update(float delta, TiledMapTileLayer layer) {
        // horizontal movement
        isGrounded = false;
        float oldX = x;
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

        // apply velocityX for knockback when player is hit
        x += velocityX * delta;
        velocityX *= 0.9f; //slowdown for accurate knockback simulation

        //invincibility counter after being hit
        if (invincibilityTimer > 0) {
            invincibilityTimer -= delta;
        }

        //apply gravity
        velocityY += GRAVITY * delta;
        float oldY = y;
        y += velocityY * delta;

        //keeping bounds in sync with player pos
        bounds.setPosition(x, y);
        if (isColliding(layer)) {
            if (velocityY<0) {
                // falling down and hitting a layer
                isGrounded = true;
            }
            y = oldY;
            velocityY = 0;
        }

        //jumping
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && isGrounded) {
            velocityY = JUMP_VELOCITY;
        }

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

        //attacking
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isAttacking) {
            isAttacking = true;
            attackTimer = ATTACK_DURATION;
            stateTime = 0f; //for animation reset
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

    private boolean isColliding(TiledMapTileLayer layer) {
        int startX = (int)(bounds.x / layer.getTileWidth());
        int endX = (int)((bounds.x + bounds.width) / layer.getTileWidth());
        int startY = (int)(bounds.y / layer.getTileHeight());
        int endY = (int)((bounds.y + bounds.height) / layer.getTileHeight());

        for (int tileY= startY ; tileY <= endY; tileY++) {
            for (int tileX = startX; tileX <= endX; tileX++) {
                TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
                if (cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey("solid")) {
                    return true;
                }
            }
        }
        return false;
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

    public TextureRegion getCurrentFrame(float delta) {
        stateTime += delta;

        Animation<TextureRegion> anim;
        switch (currentState) {
            case WALK: anim = walkAnim; break;
            case JUMP: anim = jumpAnim; break;
            case FALL: anim = fallAnim; break;
            case PUNCH: anim = punchAnim; break;
            default: anim = idleAnim; break;
        }

        // player facing left/right
        TextureRegion frame = anim.getKeyFrame(stateTime, true);

        if (facingRight && frame.isFlipX()) {
            frame.flip(true, false);
        } else if (!facingRight && !frame.isFlipX()) {
            frame.flip(true, false);
        }
        return frame;
    }

    public void applyKnockback(float directionX) {
        velocityX = directionX * 1000f;
        velocityY = 100f;
    }

    //state restoration
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

    //replay frame getter (no delta advance, uses stateTime)
    public TextureRegion getCurrentFrameForReplay() {
        Animation<TextureRegion> anim;
        switch (currentState) {
            case WALK: anim = walkAnim; break;
            case JUMP: anim = jumpAnim; break;
            case FALL: anim = fallAnim; break;
            case PUNCH: anim = punchAnim; break;
            default: anim = idleAnim; break;
        }

        //use recorded stateTime
        boolean loop = currentState != State.PUNCH;
        TextureRegion frame = anim.getKeyFrame(stateTime, loop);

        if (facingRight && frame.isFlipX()) frame.flip(true, false);
        else if (!facingRight && !frame.isFlipX()) frame.flip(true, false);
        return frame;
    }

    public boolean isInvincible() {
        return invincibilityTimer > 0;
    }

    public void triggerInvincibility() {
        invincibilityTimer = INVINCIBILITY_DURATION;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getSpeed() {
        return speed;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public Player.State getCurrentState() {
        return currentState;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public float getStateTime() {
        return stateTime;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }

    public void takeDamage(int amount) {
        if (!isInvincible()) {
            currentHealth -= amount;
            triggerInvincibility();
        }
    }
}
