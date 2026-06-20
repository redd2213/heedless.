package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player {
    private float x = 100;
    private float y = 100;
    private float speed = 200;
    private float velocityY = 0;
    private boolean isGrounded = false;
    private Texture texture;
    private Rectangle bounds = new Rectangle();
    private static final float GRAVITY = -500f;
    private static final float JUMP_VELOCITY = 430f;

    //animation fields
    public enum State { IDLE, WALK, JUMP, FALL }
    private State currentState = State.IDLE;
    private float stateTime = 0f;
    private boolean facingRight = true;
    public static final int FRAME_WIDTH = 82;
    public static final int FRAME_HEIGHT = 60;

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> jumpAnim;
    private Animation<TextureRegion> fallAnim;

    public Player() {
        idleAnim = loadAnimation("Assets/SPRITES/player/Idle/spritesheet.png", 4, 0.15f);
        walkAnim = loadAnimation("Assets/SPRITES/player/Walk/spritesheet.png", 6, 0.1f);
        jumpAnim = loadAnimation("Assets/SPRITES/player/Jump/spritesheet.png", 2, 0.1f);
        fallAnim = loadAnimation("Assets/SPRITES/player/Fall/spritesheet.png", 2, 0.15f);

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

        if (!isGrounded && velocityY > 0) {
            currentState = State.JUMP;
        } else if (!isGrounded && velocityY < 0) {
            currentState = State.FALL;
        } else if (x != oldX) {
            currentState = State.WALK;
        } else {
            currentState = State.IDLE;
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

    public boolean isGrounded() {
        return isGrounded;
    }

    public Texture getTexture() {
        return texture;
    }
}
