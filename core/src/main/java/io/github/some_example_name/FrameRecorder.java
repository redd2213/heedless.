package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;

/**
 * Records a complete snapshot of the game state every frame during gameplay.
 * Uses a state-based approach rather than input-based recording to achieve
 * frame-perfect replay accuracy regardless of delta time variation.
 *
 * <p>Single-frame events (jump, attack) must be explicitly signalled via
 * {@link #onJumpPressed()} and {@link #onAttackPressed()} before calling
 * {@link #recordFrame} each frame, since these events cannot be reliably
 * detected from continuous key polling.
 */
public class FrameRecorder {
    private Array<FrameState> frames = new Array<>();
    private float timer = 0f;
    private boolean recording = false;

    private boolean jumpPressedThisFrame = false;
    private boolean attackPressedThisFrame = false;

    /**
     * Signals that the player pressed jump this frame.
     * Must be called before {@link #recordFrame} to be captured correctly.
     */
    public void onJumpPressed() {
        jumpPressedThisFrame = true;
    }

    /**
     * Signals that the player pressed attack this frame.
     * Must be called before {@link #recordFrame} to be captured correctly.
     */
    public void onAttackPressed() {
        attackPressedThisFrame = true;
    }

    /**
     * Starts a new recording session, clearing any previously recorded frames.
     */
    public void startRecording() {
        frames.clear();
        timer = 0f;
        recording = true;
    }

    /**
     * Stops the current recording session.
     * Recorded frames remain accessible via {@link #getFrames()}.
     */
    public void stopRecording() {
        recording = false;
    }

    /**
     * Returns whether recording is currently active.
     *
     * @return true if currently recording frames
     */
    public boolean isRecording() {
        return recording;
    }

    /**
     * Records a complete game state snapshot for the current frame.
     * Should be called once per frame after all game logic has been updated.
     * Does nothing if recording has not been started or has been stopped.
     *
     * @param delta       time elapsed since last frame in seconds
     * @param player      the player whose state to record
     * @param enemy       the enemy whose state to record
     * @param collectibles the array of collectibles whose states to record
     */
    public void recordFrame(float delta, Player player, Enemy enemy,
                            Collectible[] collectibles) {
        if (!recording) return;

        timer += delta;

        FrameState fs = new FrameState();
        fs.timestamp = timer;
        fs.delta = delta;

        // input
        fs.leftHeld = Gdx.input.isKeyPressed(Input.Keys.A);
        fs.rightHeld = Gdx.input.isKeyPressed(Input.Keys.D);
        fs.jumpPressed = jumpPressedThisFrame;
        fs.attackPressed = attackPressedThisFrame;

        // player
        fs.playerX = player.getX();
        fs.playerY = player.getY();
        fs.velocityY = player.getVelocityY(); // fixed: was incorrectly getVelocityX()
        fs.velocityX = player.getVelocityX();
        fs.isGrounded = player.isGrounded();
        fs.animState = player.getCurrentState();
        fs.facingRight = player.isFacingRight();
        fs.stateTime = player.getStateTime();
        fs.playerHealth = player.getCurrentHealth();

        // collectibles
        if (collectibles != null && collectibles.length >= 3) {
            fs.stone0 = collectibles[0].isCollected();
            fs.stone1 = collectibles[1].isCollected();
            fs.stone2 = collectibles[2].isCollected();
        }

        // enemy
        fs.enemyX = enemy.getX();
        fs.enemyDead = enemy.isDead();
        fs.enemyHealth = enemy.getCurrentHealth();

        frames.add(fs);

        // reset single-frame flags
        jumpPressedThisFrame = false;
        attackPressedThisFrame = false;
    }

    /**
     * Returns all recorded frame snapshots in chronological order.
     *
     * @return the array of recorded {@link FrameState} objects
     */
    public Array<FrameState> getFrames() {
        return frames;
    }

    /**
     * Returns the total elapsed recording time in seconds.
     *
     * @return elapsed time since {@link #startRecording()} was called
     */
    public float getTimer() {
        return timer;
    }
}
