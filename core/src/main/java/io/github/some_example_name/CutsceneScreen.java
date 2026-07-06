package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Displays the opening cutscene of Heedless.
 * A girl is chased across the screen by a werewolf, setting up the game's narrative context.
 * After both characters exit the screen, a 2-second pause occurs before transitioning
 * to the {@link StoryScreen}. The player can skip the cutscene at any time by pressing ESC.
 */
public class CutsceneScreen implements Screen {
    private Main game;
    private SpriteBatch batch;

    // girl
    private Animation<TextureRegion> girlAnim;
    private float girlX;
    private float girlY;
    private Texture girlSheet;

    // werewolf
    private Animation<TextureRegion> wolfAnim;
    private float wolfX;
    private float wolfY;
    private Texture wolfSheet;

    private float stateTime = 0f;
    private float speed = 300f;
    private float pauseTimer = 0f;
    private boolean charactersDone = false;

    // scale factor to size the characters on the cutscene screen
    private static final float GIRL_SCALE = 3.5f;
    private static final float WOLF_SCALE = 3.0f;

    // actual draw size
    private static final float GIRL_WIDTH = 39 * GIRL_SCALE;
    private static final float GIRL_HEIGHT = 53 * GIRL_SCALE;
    private static final float WOLF_WIDTH = 96 * WOLF_SCALE;
    private static final float WOLF_HEIGHT = 76 * WOLF_SCALE;

    // gap between characters
    private static final float CHASE_GAP = 500f;

    // text display
    private Label textLabel;
    private Stage hudStage;
    private Skin skin;
    private Label skipLabel;
    private float blinkTimer = 0f;
    private boolean blinkVisible = true;

    /**
     * Creates a new CutsceneScreen.
     *
     * @param game the main game instance used for screen transitions
     */
    public CutsceneScreen(Main game) {
        this.game = game;
    }

    /**
     * Loads a sprite sheet from the given path and splits it into an Animation.
     *
     * @param path          internal asset path to the sprite sheet PNG
     * @param cols          number of columns (frames) in the sprite sheet
     * @param frameDuration duration of each frame in seconds
     * @return the constructed looping Animation
     */
    private Animation<TextureRegion> loadAnimation(String path, int cols, float frameDuration) {
        Texture sheet = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tmp = TextureRegion.split(sheet,
            sheet.getWidth() / cols, sheet.getHeight());
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < cols; i++) {
            frames.add(tmp[0][i]);
        }
        return new Animation<>(frameDuration, frames);
    }

    /**
     * Initializes all assets and positions for the cutscene.
     * Loads the girl and werewolf animations, sets starting positions off-screen left,
     * and prepares the HUD elements including the blinking skip label.
     */
    @Override
    public void show() {
        batch = new SpriteBatch();

        girlSheet = new Texture(Gdx.files.internal(
            "Assets/SPRITES/Dancing Girl Files/spritesheets/skip.png"));
        TextureRegion[][] girlTmp = TextureRegion.split(girlSheet,
            girlSheet.getWidth() / 8, girlSheet.getHeight());
        Array<TextureRegion> girlFrames = new Array<>();
        for (int i = 0; i < 8; i++) girlFrames.add(girlTmp[0][i]);
        girlAnim = new Animation<>(0.1f, girlFrames);

        wolfSheet = new Texture(Gdx.files.internal(
            "Assets/SPRITES/WereWolf/Spritesheets/werewolf-run.png"));
        TextureRegion[][] wolfTmp = TextureRegion.split(wolfSheet,
            wolfSheet.getWidth() / 6, wolfSheet.getHeight());
        Array<TextureRegion> wolfFrames = new Array<>();
        for (int i = 0; i < 6; i++) wolfFrames.add(wolfTmp[0][i]);
        wolfAnim = new Animation<>(0.1f, wolfFrames);

        // girl starts from left side of screen and wolf further behind
        girlX = -GIRL_WIDTH;
        wolfX = girlX - CHASE_GAP;

        // vertical center on screen
        float floorY = Gdx.graphics.getHeight() / 2f - WOLF_HEIGHT / 2;
        girlY = floorY;
        wolfY = floorY;

        // text display
        hudStage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));
        textLabel = new Label("HELP ME !!!", skin);
        textLabel.setFontScale(1.3f);
        hudStage.addActor(textLabel);

        // skip button overlay
        skipLabel = new Label("Press ESC to skip", skin);
        skipLabel.setFontScale(0.8f);
        hudStage.addActor(skipLabel);

        Gdx.input.setInputProcessor(null);
    }

    /**
     * Updates and renders the cutscene each frame.
     * Moves both characters across the screen at the same speed maintaining the chase gap.
     * Once both characters have exited, waits 2 seconds then transitions to {@link StoryScreen}.
     * Handles ESC key for instant skip and renders the blinking skip label.
     *
     * @param delta time elapsed since last frame in seconds
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        // ESC key for instant skip
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new StoryScreen(game));
            return;
        }

        stateTime += delta;

        if (!charactersDone) {
            // move both characters at the same speed
            girlX += speed * delta;
            wolfX += speed * delta;

            // start pause timer once wolf exits screen
            if (wolfX > Gdx.graphics.getWidth()) {
                charactersDone = true;
            }
        } else {
            pauseTimer += delta;
            if (pauseTimer >= 2f) {
                game.setScreen(new StoryScreen(game));
                return;
            }
        }

        batch.begin();

        // draw wolf first (behind girl)
        TextureRegion wolfFrame = wolfAnim.getKeyFrame(stateTime, true);
        batch.draw(wolfFrame, wolfX, wolfY, WOLF_WIDTH, WOLF_HEIGHT);

        // draw girl on top
        TextureRegion girlFrame = girlAnim.getKeyFrame(stateTime, true);

        // flip texture to face the right way
        if (!girlFrame.isFlipX()) {
            girlFrame.flip(true, false);
        }

        batch.draw(girlFrame, girlX, girlY, GIRL_WIDTH, GIRL_HEIGHT);

        batch.end();

        // blinking logic for the skip label
        blinkTimer += delta;
        if (blinkTimer >= 0.6f) {
            blinkVisible = !blinkVisible;
            blinkTimer = 0f;
        }

        // opacity for skip label
        skipLabel.setColor(1, 1, 1, blinkVisible ? 0.6f : 0f);

        // draw text following girl
        textLabel.setPosition(girlX, girlY + GIRL_HEIGHT);
        hudStage.act(delta);
        hudStage.draw();
    }

    /**
     * Handles window resize events by updating the HUD viewport
     * and repositioning the skip label to the bottom right corner.
     *
     * @param width  new window width in pixels
     * @param height new window height in pixels
     */
    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (hudStage != null) {
            hudStage.getViewport().update(width, height, true);
            float actualTextWidth = skipLabel.getPrefWidth() * skipLabel.getFontScaleX();
            skipLabel.setPosition(width - actualTextWidth - 50, 20);
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    /**
     * Disposes of all assets used by this screen to free memory.
     * Safely checks for null before disposing stage and skin.
     */
    @Override
    public void dispose() {
        batch.dispose();
        if (girlSheet != null) girlSheet.dispose();
        if (wolfSheet != null) wolfSheet.dispose();
        if (hudStage != null) hudStage.dispose();
        if (skin != null) skin.dispose();
    }
}
