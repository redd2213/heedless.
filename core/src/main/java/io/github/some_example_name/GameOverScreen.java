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
 * Displays the Game Over screen when the player's health reaches zero.
 * Features a Dark Souls inspired "YOU DIED." title that fades in slowly,
 * followed by a "Return to Menu" button. Plays a dedicated music track.
 */
public class GameOverScreen implements Screen {
    private Main game;
    private Stage stage;
    private Skin skin;
    private float titleAlpha = 0f;
    private float buttonAlpha = 0f;
    private Label titleLabel;
    private TextButton rtmButton;
    private Music music;

    /**
     * Creates a new GameOverScreen.
     *
     * @param game the main game instance used for screen navigation
     */
    public GameOverScreen(Main game) {
        this.game = game;
    }

    /**
     * Initializes the Game Over UI with a title label and return to menu button.
     * Starts the game over music track.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        // title label
        titleLabel = new Label("YOU DIED.", skin, "title", Color.RED);
        table.add(titleLabel).padBottom(150).row();

        // return to menu button
        rtmButton = new TextButton("Return to menu", skin);
        rtmButton.getLabel().setFontScale(0.7f);
        table.add(rtmButton).size(300, 100).row();
        rtmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        // music
        music = Gdx.audio.newMusic(Gdx.files.internal(
            "Assets/Music (Crimson Hollow by Andy Martinez on itch.io)/Point of return(GameOverScreen).wav"));
        music.setLooping(true);
        music.setVolume(0.5f);
        music.play();

        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Renders the Game Over screen with a gradual fade-in effect.
     * The title fades in first, then the button appears once the title
     * reaches 40% opacity.
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
        titleLabel.setColor(1, 0, 0, titleAlpha);
        rtmButton.setColor(1, 1, 1, buttonAlpha);
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
