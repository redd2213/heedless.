package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;

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

    public Player() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();

        bounds.setSize(32, 32);
        bounds.setPosition(x, y);
    }

    public void update(float delta, TiledMapTileLayer layer) {
        // horizontal movement
        isGrounded = false;
        float oldX = x;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += speed * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= speed * delta;
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
