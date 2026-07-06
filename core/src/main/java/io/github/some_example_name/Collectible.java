package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Represents a collectible Energy Stone in the game world.
 * Each stone has a position, an animated sprite, and a collected state.
 * When all stones are collected alongside defeating all enemies,
 * the portal to the next area is activated.
 */
public class Collectible {
    private float x;
    private float y;
    private boolean collected = false;
    private Rectangle bounds = new Rectangle();
    private Animation<TextureRegion> energyStone;
    private float stateTime = 0f;
    private Texture spriteSheet;

    /** Rendered size of the collectible in pixels. */
    public static final int SIZE = 32;

    /**
     * Creates a new Collectible at the specified position.
     * Loads the energy stone animation from the sprite sheet.
     *
     * @param x the x pixel coordinate in the game world
     * @param y the y pixel coordinate in the game world
     */
    public Collectible(float x, float y) {
        this.x = x;
        this.y = y;
        energyStone = loadAnimation(
            "Assets/SPRITES/Grotto-escape-2-FX/spritesheets/energy-shield.png",
            8, 0.1f);
        bounds.set(x, y, SIZE, SIZE);
    }

    /**
     * Updates the animation timer for this collectible.
     * Only advances if the stone has not yet been collected.
     *
     * @param delta time elapsed since last frame in seconds
     */
    public void update(float delta) {
        if (!collected) {
            stateTime += delta;
        }
    }

    /**
     * Loads a sprite sheet and splits it into an animation.
     *
     * @param path          internal asset path to the sprite sheet
     * @param cols          number of columns (frames) in the sprite sheet
     * @param frameDuration duration of each frame in seconds
     * @return the constructed Animation object
     */
    private Animation<TextureRegion> loadAnimation(String path, int cols, float frameDuration) {
        spriteSheet = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet,
            spriteSheet.getWidth() / cols, spriteSheet.getHeight());
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < cols; i++) {
            frames.add(tmp[0][i]);
        }
        return new Animation<>(frameDuration, frames);
    }

    /**
     * Returns the current animation frame for rendering.
     *
     * @return the current TextureRegion frame
     */
    public TextureRegion getCurrentFrame() {
        return energyStone.getKeyFrame(stateTime, true);
    }

    /**
     * Returns whether this stone has been collected by the player.
     *
     * @return true if collected, false otherwise
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Marks this stone as collected.
     * Once collected it can no longer be picked up again.
     */
    public void collect() {
        collected = true;
    }

    /**
     * Returns the collision bounds of this collectible.
     *
     * @return the Rectangle representing the hitbox
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Returns the x coordinate of this collectible in the game world.
     *
     * @return x position in pixels
     */
    public float getX() { return x; }

    /**
     * Returns the y coordinate of this collectible in the game world.
     *
     * @return y position in pixels
     */
    public float getY() { return y; }

    /**
     * Disposes of the sprite sheet texture to free GPU memory.
     * Must be called when the collectible is no longer needed.
     */
    public void dispose() {
        if (spriteSheet != null) spriteSheet.dispose();
    }
}
