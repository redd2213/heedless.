package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    private SpriteBatch batch;
    private Texture playerTexture;
    private float playerX = 100;
    private float playerY = 100;
    private float speed = 200; // pixels per second
    private OrthographicCamera camera;
    @Override
    public void show() {
        // Prepare your screen here.
        batch = new SpriteBatch();
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        playerTexture = new Texture(pixmap);
        pixmap.dispose();
        Gdx.input.setInputProcessor(null);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerX += speed * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerX -= speed * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerY += speed * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerY -= speed * delta;
        }

        playerX = MathUtils.clamp(playerX, 0, Gdx.graphics.getWidth() - playerTexture.getWidth());
        playerY = MathUtils.clamp(playerY, 0, Gdx.graphics.getHeight() - playerTexture.getHeight());

        ScreenUtils.clear(Color.BLACK);

        camera.position.set(playerX, playerY, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(playerTexture, playerX, playerY);
        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
}
