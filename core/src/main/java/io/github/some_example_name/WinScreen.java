package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Displayed when the player completes the level by collecting all Energy Stones,
 * defeating all enemies, and entering the portal.
 * Features a "RUN COMPLETED." title that fades in, followed by buttons
 * to return to the menu or watch the frame-perfect replay of the completed run.
 */
public class WinScreen implements Screen {
    private Main game;
    private Stage stage;
    private Skin skin;
    private float titleAlpha = 0f;
    private float buttonAlpha = 0f;
    private Label titleLabel;
    private TextButton rtmButton;
    private TextButton replayButton;
    private Music music;

    /**
     * Creates a new WinScreen.
     *
     * @param game the main game instance used for screen navigation
     */
    public WinScreen(Main game) {
        this.game = game;
    }

    /**
     * Initializes the Win screen UI with title, return to menu button,
     * and watch replay button. Starts the win music track.
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
        titleLabel = new Label("RUN COMPLETED.", skin, "title");
        table.add(titleLabel).padBottom(50).row();

        // return to menu button
        rtmButton = new TextButton("Return to menu", skin);
        rtmButton.getLabel().setFontScale(0.7f);
        table.add(rtmButton).size(300, 100).padBottom(40).row();
        rtmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        // replay button
        replayButton = new TextButton("Watch Replay", skin);
        replayButton.getLabel().setFontScale(0.7f);
        table.add(replayButton).size(300, 100).row();
        replayButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ReplayScreen(game, game.currentUserId));
            }
        });

        // music
        music = Gdx.audio.newMusic(Gdx.files.internal(
            "Assets/Music (Crimson Hollow by Andy Martinez on itch.io)/Cherry Orchard(WinScreen).wav"));
        music.setLooping(true);
        music.setVolume(0.5f);
        music.play();

        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Renders the Win screen with a gradual fade-in effect.
     * The title fades in first, then buttons appear once title reaches 40% opacity.
     *
     * @param delta time elapsed since last frame in seconds
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();

        titleAlpha = Math.min(titleAlpha + delta * 0.2f, 1f);
        if (titleAlpha >= 0.4f) {
            buttonAlpha = Math.min(buttonAlpha + delta * 0.5f, 1f);
        }
        titleLabel.setColor(1, 1, 1, titleAlpha);
        rtmButton.setColor(1, 1, 1, buttonAlpha);
        replayButton.setColor(1, 1, 1, buttonAlpha);
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

    /**
     * Stops and disposes the music when another screen replaces this one.
     */
    @Override
    public void hide() {
        music.stop();
        music.dispose();
    }

    /**
     * Disposes of all assets. Safely checks music for null before disposing.
     */
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        if (music != null) {
            music.stop();
            music.dispose();
        }
    }
}
