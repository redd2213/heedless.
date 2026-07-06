package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Represents the portal that transports the player to the next area.
 * The portal remains invisible and inactive until all win conditions are met
 * (all Energy Stones collected and all enemies defeated), at which point
 * it activates and begins animating. Entering the portal triggers the win state.
 */
public class Portal {
    private Animation<TextureRegion> animation;
    private float stateTime = 0f;
    private float x;
    private float y;
    private Rectangle bounds;
    private boolean active = false;

    /** Rendered width of the portal in pixels. */
    public static final float WIDTH = 32 * 2f;

    /** Rendered height of the portal in pixels. */
    public static final float HEIGHT = 32 * 2f;

    /**
     * Creates a new Portal at the specified position.
     * Loads the portal animation from the sprite sheet.
     * The portal starts inactive and invisible until {@link #activate()} is called.
     *
     * @param x the x pixel coordinate in the game world
     * @param y the y pixel coordinate in the game world
     */
    public Portal(float x, float y) {
        this.x = x;
        this.y = y;

        Texture sheet = new Texture(Gdx.files.internal(
            "Assets/SPRITES/Hits-6/spritesheet.png"));
        TextureRegion[][] tmp = TextureRegion.split(sheet,
            sheet.getWidth() / 7, sheet.getHeight());
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < 7; i++) {
            frames.add(tmp[0][i]);
        }
        animation = new Animation<>(0.1f, frames);
        bounds = new Rectangle(x, y, WIDTH, HEIGHT);
    }

    /**
     * Activates the portal, making it visible and collidable.
     * Should be called when all win conditions are satisfied.
     */
    public void activate() {
        active = true;
    }

    /**
     * Returns whether the portal is currently active and visible.
     *
     * @return true if the portal has been activated
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Advances the portal animation timer when active.
     * Does nothing if the portal has not been activated.
     *
     * @param delta time elapsed since last frame in seconds
     */
    public void update(float delta) {
        if (active) stateTime += delta;
    }

    /**
     * Draws the portal animation at its world position.
     * Does nothing if the portal is not active.
     *
     * @param batch the SpriteBatch to draw with
     * @param delta time elapsed since last frame in seconds (unused, kept for API consistency)
     */
    public void draw(SpriteBatch batch, float delta) {
        if (!active) return;
        TextureRegion frame = animation.getKeyFrame(stateTime, true);
        batch.draw(frame, x, y, WIDTH, HEIGHT);
    }

    /**
     * Returns the collision bounds of the portal.
     * Only relevant when the portal is active.
     *
     * @return the {@link Rectangle} representing the portal's hitbox
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Placeholder dispose method.
     * The portal's sprite sheet texture is currently not tracked for disposal.
     */
    public void dispose() {
        // texture disposed separately
    }
}
