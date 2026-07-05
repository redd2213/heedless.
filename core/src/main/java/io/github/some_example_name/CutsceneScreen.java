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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class CutsceneScreen implements Screen {
    private Main game;
    private SpriteBatch batch;

    //girl
    private Animation<TextureRegion> girlAnim;
    private float girlX;
    private float girlY;

    //werewolf
    private Animation<TextureRegion> wolfAnim;
    private float wolfX;
    private float wolfY;

    private float stateTime = 0f;
    private float speed = 300f;
    private float pauseTimer = 0f;
    private boolean charactersDone = false;

    //scale factor to size the characters on the cutscene screen
    private static final float GIRL_SCALE = 3.5f;
    private static final float WOLF_SCALE = 3.0f;

    //actual draw size
    private static final float GIRL_WIDTH = 39 * GIRL_SCALE;
    private static final float GIRL_HEIGHT = 53 * GIRL_SCALE;
    private static final float WOLF_WIDTH = 96 * WOLF_SCALE;
    private static final float WOLF_HEIGHT = 76 * WOLF_SCALE;

    //gap between characters
    private static final float CHASE_GAP = 500f;

    //text display
    private Label textLabel;
    private Stage hudStage;
    private Skin skin;

    public CutsceneScreen(Main game) {
        this.game = game;
    }

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

    @Override
    public void show() {
        batch = new SpriteBatch();

        girlAnim = loadAnimation("Assets/SPRITES/Dancing Girl Files/spritesheets/skip.png", 8, 0.1f);

        wolfAnim = loadAnimation("Assets/SPRITES/WereWolf/Spritesheets/werewolf-run.png", 6, 0.1f);

        //girl starts from left side of screen and wolf further behind
        girlX = -GIRL_WIDTH;
        wolfX = girlX - CHASE_GAP;

        //vertical center on screen
        float floorY = Gdx.graphics.getHeight() / 2f - WOLF_HEIGHT / 2;
        girlY = floorY;
        wolfY = floorY;

        //text display
        hudStage = new Stage();
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));
        textLabel = new Label("HELP ME !!!", skin, "title");
        textLabel.setFontScale(0.4f);
        hudStage.addActor(textLabel);

        Gdx.input.setInputProcessor(null);
    }

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
            //set same speed for both char
            girlX += speed * delta;
            wolfX += speed * delta;

            //start pause timer once girl exits screen
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
        batch.draw(wolfFrame,
            wolfX, wolfY,
            WOLF_WIDTH, WOLF_HEIGHT);

        // draw girl on top
        TextureRegion girlFrame = girlAnim.getKeyFrame(stateTime, true);

        //flip texture to face the right way
        if (!girlFrame.isFlipX()){
            girlFrame.flip(true, false);
        }

        batch.draw(girlFrame,
            girlX, girlY,
            GIRL_WIDTH, GIRL_HEIGHT);

        batch.end();

        //draw text following girl
        textLabel.setPosition(girlX, girlY + GIRL_HEIGHT);
        hudStage.act(delta);
        hudStage.draw();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        if (hudStage != null) hudStage.dispose();
        if (skin != null) skin.dispose();
    }
}
