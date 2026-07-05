package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;

public class FrameRecorder {
    private Array<FrameState> frames = new Array<>();
    private float timer = 0f;
    private boolean recording = false;

    private boolean jumpPressedThisFrame = false;
    private boolean attackPressedThisFrame = false;

    public void onJumpPressed() {
        jumpPressedThisFrame = true;
    }

    public void onAttackPressed() {
        attackPressedThisFrame = true;
    }

    public void startRecording() {
        frames.clear();
        timer = 0f;
        recording = true;
    }

    public void stopRecording() {
        recording = false;
    }

    public boolean isRecording() {
        return recording;
    }

    public void recordFrame(float delta, Player player, Enemy enemy, Collectible[] collectibles) {
        if (!recording) return;

        timer += delta;

        FrameState fs = new FrameState();
        fs.timestamp = timer;
        fs.delta = delta;

        //Input
        fs.leftHeld = Gdx.input.isKeyPressed(Input.Keys.A);
        fs.rightHeld = Gdx.input.isKeyPressed(Input.Keys.D);
        fs.jumpPressed = jumpPressedThisFrame;
        fs.attackPressed = attackPressedThisFrame;

        //Player
        fs.playerX = player.getX();
        fs.playerY = player.getY();
        fs.velocityY = player.getVelocityX();
        fs.isGrounded = player.isGrounded();
        fs.animState = player.getCurrentState();
        fs.facingRight = player.isFacingRight();
        fs.stateTime = player.getStateTime();
        fs.playerHealth = player.getCurrentHealth();

        //Collectibles
        if (collectibles != null && collectibles.length >= 3) {
            fs.stone0 = collectibles[0].isCollected();
            fs.stone1 = collectibles[1].isCollected();
            fs.stone2 = collectibles[2].isCollected();
        }

        //Enemy
        fs.enemyX = enemy.getX();
        fs.enemyDead = enemy.isDead();
        fs.enemyHealth = enemy.getCurrentHealth();

        frames.add(fs);

        //Reset single-frame flags
        jumpPressedThisFrame = false;
        attackPressedThisFrame = false;
    }

    public Array<FrameState> getFrames() {
        return frames;
    }

    public float getTimer() {
        return timer;
    }

}
