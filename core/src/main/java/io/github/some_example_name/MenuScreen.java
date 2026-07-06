package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * The main menu screen for Heedless.
 * Displays the game title with Play and Quit buttons.
 * Play navigates to {@link CutsceneScreen} to begin the game flow.
 * The persistent menu music from {@link Main#menuMusic} is restarted
 * here if returning from another screen such as {@link WinScreen} or {@link GameOverScreen}.
 */
public class MenuScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private Main game;

    /**
     * Creates a new MenuScreen.
     *
     * @param game the main game instance used for screen navigation
     */
    public MenuScreen(Main game) {
        this.game = game;
    }

    /**
     * Initializes the menu UI with title label, Play and Quit buttons.
     * Restarts the menu music if it is not already playing.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        // title
        Label titleLabel = new Label("Heedless.", skin, "title");
        table.add(titleLabel).padBottom(50).row();

        // play button
        TextButton playButton = new TextButton("Play", skin);
        table.add(playButton).size(250, 100).padBottom(40).row();
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new CutsceneScreen(game));
            }
        });

        // quit button
        TextButton quitButton = new TextButton("Quit", skin);
        table.add(quitButton).size(250, 100).row();
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // restart menu music if returning from another screen
        if (game.menuMusic != null && !game.menuMusic.isPlaying()) {
            game.menuMusic.play();
        }

        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Clears the screen and renders the menu UI each frame.
     *
     * @param delta time elapsed since last frame in seconds
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
    }

    /**
     * Updates the stage viewport on window resize.
     *
     * @param width  new window width in pixels
     * @param height new window height in pixels
     */
    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    /**
     * Disposes of stage and skin assets.
     * Menu music is managed by {@link Main} and disposed there.
     */
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
