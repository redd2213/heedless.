package io.github.some_example_name;

/**
 * A single snapshot of the complete game state at one point in time.
 * Recorded every frame by {@link FrameRecorder} during gameplay and
 * persisted to the database by {@link DatabaseManager}.
 * During replay, {@link ReplayScreen} restores state directly from these
 * snapshots — no physics simulation — achieving frame-perfect playback.
 */
public class FrameState {
    /** Time in seconds since the recording started. */
    public float timestamp;

    /** Duration of this frame in seconds. Used to advance animations during replay. */
    public float delta;

    /** Whether the A (left) key was held this frame. */
    public boolean leftHeld;

    /** Whether the D (right) key was held this frame. */
    public boolean rightHeld;

    /** Whether jump was triggered this frame (single-frame event). */
    public boolean jumpPressed;

    /** Whether attack was triggered this frame (single-frame event). */
    public boolean attackPressed;

    /** Player x position in pixels. */
    public float playerX;

    /** Player y position in pixels. */
    public float playerY;

    /** Player vertical velocity in pixels per second. */
    public float velocityY;

    /** Player horizontal velocity in pixels per second (used for knockback). */
    public float velocityX;

    /** Whether the player was grounded this frame. */
    public boolean isGrounded;

    /** The player's animation state this frame. */
    public Player.State animState;

    /** Whether the player was facing right this frame. */
    public boolean facingRight;

    /** The player's animation timer value this frame. */
    public float stateTime;

    /** The player's current health points this frame. */
    public int playerHealth;

    /** Whether the first Energy Stone was collected by this frame. */
    public boolean stone0;

    /** Whether the second Energy Stone was collected by this frame. */
    public boolean stone1;

    /** Whether the third Energy Stone was collected by this frame. */
    public boolean stone2;

    /** Enemy x position in pixels this frame. */
    public float enemyX;

    /** Whether the enemy was dead by this frame. */
    public boolean enemyDead;

    /** Enemy current health points this frame. */
    public int enemyHealth;

    /** Default constructor required for database deserialization. */
    public FrameState() {}
}
