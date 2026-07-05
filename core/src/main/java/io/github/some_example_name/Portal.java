package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Portal {
    private Animation<TextureRegion> animation;
    private float stateTime = 0f;
    private float x;
    private float y;
    private Rectangle bounds;
    private boolean active = false;

    //scaling
    public static final float WIDTH = 32 * 2f;
    public static final float HEIGHT = 32 * 2f;

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

    public void activate() {
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void update(float delta) {
        if (active) stateTime += delta;
    }

    public void draw(SpriteBatch batch, float delta) {
        if (!active) return;
        TextureRegion frame = animation.getKeyFrame(stateTime, true);
        batch.draw(frame, x, y, WIDTH, HEIGHT);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        // texture disposed separately
    }
}

