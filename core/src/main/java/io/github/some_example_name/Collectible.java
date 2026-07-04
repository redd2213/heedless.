package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Collectible {
    private float x;
    private float y;
    private boolean collected = false;
    private Texture texture;
    private Rectangle bounds = new Rectangle();

    public static final int SIZE = 16;

    public Collectible(float x, float y) {
        this.x = x;
        this.y =  y;

        //placeholder yellow square that is to be replaced with a sprite later
        Pixmap pixmap = new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.YELLOW);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();

        bounds.set(x, y, SIZE, SIZE);
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public void dispose() {
        texture.dispose();
    }
}
