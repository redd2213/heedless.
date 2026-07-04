package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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

/** First screen of the application. Displayed after the application is created. */
public class WinScreen implements Screen {
    private Main game;
    private Stage stage;
    private Skin skin;
    private float titleAlpha = 0f;
    private float buttonAlpha = 0f;
    private Label titleLabel;
    private TextButton rtmButton;
    private TextButton replayButton;

    public WinScreen(Main game) {
        this.game = game;
    }
    @Override
    public void show() {
        // Prepare your screen here.
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        //title label
        titleLabel = new Label("RUN COMPLETED.", skin, "title");
        table.add(titleLabel).padBottom(150).row();

        //return to menu button
        rtmButton = new TextButton("Return to menu", skin);
        rtmButton.getLabel().setFontScale(0.7f);
        table.add(rtmButton).size(300, 100).padBottom(40).row();
        rtmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        //replay button
        replayButton = new TextButton("Watch Replay", skin);
        replayButton.getLabel().setFontScale(0.7f);
        table.add(replayButton).size(300, 100).row();
        replayButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ReplayScreen(game, 1)); // the hardcoded userId
            }
        });

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
        //alpha modifiers for fading effect (title and button)
        titleAlpha = Math.min(titleAlpha + delta * 0.2f, 1f);
        if (titleAlpha >= 0.4f) {
            buttonAlpha = Math.min(buttonAlpha + delta * 0.5f, 1f);
        }
        titleLabel.setColor(0, 1, 1, titleAlpha);
        rtmButton.setColor(1, 1, 1, buttonAlpha);
        replayButton.setColor(1, 1, 1, buttonAlpha);
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
        skin.dispose();
    }
}
