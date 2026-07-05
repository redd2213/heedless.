package io.github.some_example_name;

public class FrameState {
    //Timing
    public float timestamp;
    public float delta;

    //Input (not used for physics)
    public boolean leftHeld;
    public boolean rightHeld;
    public boolean jumpPressed;
    public boolean attackPressed;

    //Player physics
    public float playerX;
    public float playerY;
    public float velocityY;
    public float velocityX;
    public boolean isGrounded;

    //Player visual
    public Player.State animState;
    public boolean facingRight;
    public float stateTime;
    public int playerHealth;

    //collectibles
    public boolean stone0;
    public boolean stone1;
    public boolean stone2;

    //enemy
    public float enemyX;
    public boolean enemyDead;
    public int enemyHealth;

    public FrameState() {}
}
