package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Collectible {
    private float x;
    private float y;
    private boolean collected = false;
    private Rectangle bounds = new Rectangle();
    private Animation<TextureRegion> energyStone;
    private float stateTime = 0f;
    private Texture spriteSheet;

    public static final int SIZE = 16;

    public Collectible(float x, float y) {
        this.x = x;
        this.y =  y;

        //animation
        energyStone = loadAnimation("Assets/SPRITES/Grotto-escape-2-FX/spritesheets/energy-shield.png", 8, 0.1f);

        //bounds
        bounds.set(x, y, SIZE, SIZE);
    }

    public void update(float delta) {
        if (!collected) {
            stateTime += delta;
        }
    }

    private Animation<TextureRegion> loadAnimation(String path, int cols, float frameDuration) {
        spriteSheet = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / cols, spriteSheet.getHeight());
        Array<TextureRegion> frames = new Array<>();
        for (int i=0; i < cols; i++) {
            frames.add(tmp[0][i]);
        }
        return new Animation<>(frameDuration, frames);
    }

    public TextureRegion getCurrentFrame() {
        // simpler than in player because we have only one animation
        TextureRegion frame = energyStone.getKeyFrame(stateTime, true);
        return frame;
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

    public float getX() { return x; }
    public float getY() { return y; }

    public void dispose() {
        if (spriteSheet != null) spriteSheet.dispose();
    }
}
