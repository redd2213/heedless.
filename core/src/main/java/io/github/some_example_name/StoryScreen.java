package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class StoryScreen implements Screen {
    private Main game;
    private Stage stage;
    private Skin skin;

    private Label storyLabel;
    private Label continueLabel;

    private float storyAlpha = 0f;
    private float continueAlpha = 0f;
    private boolean storyFullyVisible = false;
    private boolean waitingForInput = false;

    //blink timer for "press any button"
    private float blinkTimer = 0f;
    private boolean blinkVisible = true;

    private static final String STORY_TEXT =
        "You watch the girl disappear through the abandoned church doors, the werewolf close behind her.\n\n" +
            "Something stirs in your chest.\n" +
            "It's not courage, not duty,\n" +
            "just the simple certainty that you cannot walk away.\n\n" +
            "Your fists are clenched and your feet are already moving.\n" +
            "Find her and bring her home safe.";

    public StoryScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        //text- centered and wraps at 800px
        storyLabel = new Label(STORY_TEXT, skin);
        storyLabel.setWrap(true);
        storyLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        storyLabel.setColor(1, 1, 1, 0f); // start invisible
        table.add(storyLabel).width(800).padBottom(80).row();

        //press any button label
        continueLabel = new Label("Press any key to continue.", skin);
        continueLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        continueLabel.setColor(1, 1, 1, 0f); // start invisible
        table.add(continueLabel).row();

        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        // ESC for instant skip
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new GameScreen(game));
            return;
        }

        //fade in text
        if (!storyFullyVisible) {
            storyAlpha = Math.min(storyAlpha + delta * 0.4f, 1f);
            storyLabel.setColor(1, 1, 1, storyAlpha);

            if (storyAlpha >= 1f) {
                storyFullyVisible = true;
                waitingForInput = true;
            }
        }

        //fade in "press any key" after text is visible
        if (storyFullyVisible) {
            continueAlpha = Math.min(continueAlpha + delta * 0.8f, 1f);

            //blink effect added wehn fully visible
            if (continueAlpha >= 1f) {
                blinkTimer += delta;
                if (blinkTimer >= 0.6f) {
                    blinkVisible = !blinkVisible;
                    blinkTimer = 0f;
                }
                continueLabel.setColor(1, 1, 1, blinkVisible ? 1f : 0f);
            } else {
                continueLabel.setColor(1, 1, 1, continueAlpha);
            }
        }

        //wait for any key press
        if (waitingForInput && Gdx.input.isKeyJustPressed(
            com.badlogic.gdx.Input.Keys.ANY_KEY)) {
            game.setScreen(new GameScreen(game));
            return;
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
